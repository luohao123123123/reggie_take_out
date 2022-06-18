package com.luohao.reggie.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.Setmeal;
import com.luohao.reggie.bean.SetmealDish;
import com.luohao.reggie.dto.DishDto;
import com.luohao.reggie.dto.SetmealDto;
import com.luohao.reggie.service.DishService;
import com.luohao.reggie.service.SetmealDishService;
import com.luohao.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 套餐Controller
 */
@RestController
@Slf4j
@Api(tags = "套餐Controller")
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;



    /**
     * 新增套餐
     * @param setmealDto 前端页面与后端传输的数据模型
     * @return
     * CacheEvict:可用于类或方法上；在执行完目标方法后，清除缓存中对应key的数据(如果缓存中有对应key的数据缓存的话)
     */
//    @CacheEvict(value = "setmealInfo",key = "'setmeal_'+#setmealDto.getCategoryId()+'_status_'+#setmealDto.getStatus()")  //方法执行完之后清楚缓存
    @ApiOperation(value = "新增套餐")
    @PostMapping
    public R<String>  add(@RequestBody SetmealDto setmealDto){
        //保存套餐的基本信息到套餐表
        setmealService.save(setmealDto);
        //保存套餐里面的菜品到套餐菜品关联表
            //获取套餐的关联菜品信息
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
            //给关联的菜品信息设置套餐id
        setmealDishList=setmealDishList.stream().map(x->{
            x.setSetmealId(setmealDto.getId());
            return x;
        }).collect(Collectors.toList());
            //执行保存
        setmealDishService.saveBatch(setmealDishList);

        //todo:用户套餐查看优化，如果后台新增套餐，则需要把缓存的套餐信息清除
        redisTemplate.opsForHash().delete("setmealInfo","setmeal_"+setmealDto.getCategoryId()+"_status_1");



        return  R.success("套餐保存成功");
    }


    /**
     * 套餐管理页面的分页展示
     * @return
     */

    @ApiOperation(value = "套餐信息的分页查询")
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page,int pageSize,String name){
        //执行自定义的分页方法
        Page<SetmealDto> setmealDtoPage = setmealService.dtoPageWithCategoryName(page, pageSize, name);
        //最后返回setmealDtoPage
        return R.success(setmealDtoPage);

    }


    /**
     * 套餐修改页面，需要回显套餐的信息
     * @param id
     * @return
     */
    @ApiOperation(value = "回显套餐信息")
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        //调用自定义的套餐信息获取方法
        SetmealDto setmealDto = setmealService.get(id);
        //返回SetmealDto数据
        return  R.success(setmealDto);
    }

    /**
     * 套餐信息的修改
     * @param setmealDto
     * @return
     */
//    @CacheEvict(value = "setmealInfo",key = "'setmeal_'+#setmealDto.getCategoryId()+'_status_'+#setmealDto.getStatus()")  //方法执行完之后清楚缓存
    @ApiOperation(value = "修改套餐信息")
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        //调用自定义的套餐修改方法
        setmealService.updateSetmealAndDish(setmealDto);
        //todo:用户套餐查看优化，如果后台修改套餐，则需要把缓存的套餐信息清除
        redisTemplate.opsForHash().delete("setmealInfo","setmeal_"+setmealDto.getCategoryId()+"_status_1");
        return R.success("修改成功");
    }

    /**
     * 用于套餐页面的套餐删除，需要删除套餐的基本信息和套餐中包含的菜品关系
     * @param ids
     * @return
     */
    @ApiOperation(value = "删除套餐信息")
    @DeleteMapping
    public R<String> delete(@RequestParam(value ="ids" ) List<Long> ids){
        //调用自定义的套餐删除方法，需要删除套餐的基本信息和套餐中包含的菜品关系
        setmealService.deleteWithDish(ids);

        //todo:用户套餐查看优化，如果后台删除套餐，则需要把缓存的套餐信息清除
        redisTemplate.delete("setmealInfo");
        return R.success("套餐删除成功");
    }

    /**
     * 用于套餐状态的更新
     * @param status
     * @param ids
     * @return
     */
    @ApiOperation(value = "更新套餐状态")
    @PostMapping("/status/{status}")
    public R<String> updateWithStatus(@PathVariable Integer status,@RequestParam(value = "ids") List<Long> ids){
        setmealService.updateWithStatus(status,ids);
        //todo:用户套餐查看优化，如果后台更新套餐状态，则需要把缓存的套餐信息清除
        redisTemplate.delete("setmealInfo");
        return R.success("状态更新成功");
    }

    /**
     * 用于移动端的套餐显示
     * @param setmeal
     * @return
     * Cacheable:@Cacheable：可用于类或方法上；在目标方法执行前，会根据key先去缓存中查询看是否有数据，
     *                      有就直接返回缓存中的key对应的value值。不再执行目标方法；
     *                      无则执行目标方法，并将方法的返回值作为value，并以键值对的形式存入缓存.
     */
    //todo:用户套餐查看优化，用户第一次查询套餐信息，先去redis中查询有无信息
//    @Cacheable(value = "setmealInfo",key = "'setmeal_'+#setmeal.getCategoryId()+'_status_'+#setmeal.getStatus()")
    @ApiOperation(value = "移动端的套餐查询")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //todo:用户套餐查看优化，用户第一次查询套餐信息，先去redis中查询有无信息
        //定义redis中的key
        String key="setmeal_"+setmeal.getCategoryId()+"_status_"+setmeal.getStatus();
        HashOperations<Object, Object, Object> hashType = redisTemplate.opsForHash();
        //获取redis中的缓存转为List<DishDto>
        Object redisSetmealInfo = hashType.get("setmealInfo", key);

        //todo:用户套餐查看优化，如果redis缓存中有套餐信息，则直接返回缓存中的套餐信息
        if(redisSetmealInfo!=null){
            List<Setmeal> redisSetmealInfo1 = JSON.parseArray(redisSetmealInfo.toString(), Setmeal.class);
            return R.success(redisSetmealInfo1);
        }

        //todo:用户套餐查看优化，如果redis缓存中不存在套餐信息，则直接查询数据库
        //根据套餐分类id查询所有的套餐
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        //过滤套餐status为1
        queryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        //todo:用户菜品查看优化，如果redis缓存中不存在菜品信息，则把数据库中查询到的数据缓存到redis
        hashType.put("setmealInfo",key,JSON.toJSONString(setmealList));
        redisTemplate.expire("setmealInfo",60, TimeUnit.MINUTES); //设置一个小时的过期时间

        return R.success(setmealList);
    }

    /**
     * 这里用于移动端获取套餐的全部菜品
     * @return
     */
    @ApiOperation(value = "移动端获取套餐的全部菜品")
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> getSetmealWithDish(@PathVariable(value = "id") Long setmealId){
        //调用自定义的查询套餐中菜品信息的方法
        List<DishDto> dishDtoList = setmealService.getSetmealWithDish(setmealId);

        return R.success(dishDtoList);
    }
}
