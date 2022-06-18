package com.luohao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.User;
import com.luohao.reggie.service.UserService;
import com.luohao.reggie.utils.ValidateCodeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@Api(tags = "用户Controller")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;





    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @ApiOperation(value = "发送手机验证码")
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        //判断手机号是否为kong
        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            //打印出验证码
            log.info("验证码为：{}",code);


            //将随机生成的验证码存在session中()
//            session.setAttribute(phone,code);
            //todo:短信验证码优化，之前是存在session中，session默认的保存时长是30分钟，优化后可以保存到redis中，并且设置5分钟的过期时间，当用户登录之后把短信验证码删除
            ValueOperations<String, String> stringType = stringRedisTemplate.opsForValue();
            stringType.set(phone,code,5, TimeUnit.MINUTES);

            //调用阿里云的短信服务API完成短信发送
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);  //这里因为申请阿里云的短信服务繁琐，所有只是一个示例代码
            return R.success("短信验证码发送成功");
        }
        return R.error("短信验证码发送失败");
    }


    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<Object,Object> map, HttpSession session){
        //获取前端页面提交的phone和code
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //获取存在session中的短信验证码
//        String sessionCode = session.getAttribute(phone).toString();
        //todo:短信验证码优化，这里获取存入redis中的短信验证码
        ValueOperations<String, String> stringType = stringRedisTemplate.opsForValue();
        String redisCode = stringType.get(phone);
        //如果前端填入的验证码与后端生成的验证码一致
        if(!redisCode.isEmpty() && code.equals(redisCode)){
            //todo:短信验证码优化,登录成功之后则删除保存在redis中短信验证码
            stringRedisTemplate.delete(phone);
            //判断用户是否是新用户
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user==null){  //如果数据库中没有这个user，说明是新用户，那么直接进行注册
               user=new User();
                user.setPhone(phone);
               userService.save(user); //执行保存
            }
            //把用户的id存入session中
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        else {
            return R.error("验证码错误");
        }
    }


    /**
     * 退出登录
     * @return
     */
    @ApiOperation(value = "用户退出登录")
    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }
}
