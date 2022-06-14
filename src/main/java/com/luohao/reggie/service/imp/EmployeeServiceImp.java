package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.bean.Employee;
import com.luohao.reggie.mapper.EmployeeMapper;
import com.luohao.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImp extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
