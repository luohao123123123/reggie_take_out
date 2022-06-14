package com.luohao.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.Employee;
import com.luohao.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;


import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;


/**
 * 员工管理
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码进行md5加密
        String password = employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        //2.根据username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);  //getone 是因为在数据库中对username做了唯一不能重复处理

        //3.如果没有查询到则返回登录失败结果
        if(emp==null){
            return R.error("用户不存在");
        }

        //4.进行密码比对,如果密码不一致返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        //5.查看员工状态,1为正常，0禁用
        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }

        //6.登录成功,将员工id存入session，并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }


    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> loginout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }


    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> add(@RequestBody Employee employee,HttpServletRequest request){
        log.info("新增的员工信息：{}",employee);
        //设置初始密码为123456，且进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        //设置新增员工时间
//        employee.setCreateTime(LocalDateTime.now());
        //设置更新时间
//        employee.setUpdateTime(LocalDateTime.now());
        //设置创建用户
            //获取当前员工的id
        Long empid = (Long)request.getSession().getAttribute("employee");
//        employee.setCreateUser(empid);
        //设置更新员工
//        employee.setUpdateUser(empid);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }


    /**
     * 员工信息分页展示
     * @param page  第几页
     * @param pageSize  一页有多少条
     * @param name    根据name过滤
     * @return R<Page>
     */
    @GetMapping("/page")
    public R<Page<Employee>> page(int page, int pageSize, String name){
        log.info("page:{},pagesize:{},name:{}",page,pageSize,name);

        //构造分页构造器
        Page<Employee> pageInfo=new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
            //添加过滤条件，只有当name不为空时才过滤
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
            //添加排序条件,根据更新时间从最新开始进行排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    /**
     * 根据员工id修改员工状态（禁用和启用）
     * 根据员工id修改员工信息（编辑）
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee){

        if(employee.getId()==1){
            return R.error("管理员状态无需修改");
        }
        //设置更新时间
//        employee.setUpdateTime(LocalDateTime.now());
        //设置更新员工
//        Long empid=(long)request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empid);
        //执行更新
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }


    /**
     * 编辑员工信息时，根据员工id查询员工信息，回显给输入框
     * 在输入框进行修改员工信息，点击保存之后，调用update方法进行修改和保存
     * @param id 员工id
     * @return  Employee
     */
    @GetMapping("/{id}")
    public R<Employee> getEmployeeById(@PathVariable(name = "id") Long id){
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }
}


