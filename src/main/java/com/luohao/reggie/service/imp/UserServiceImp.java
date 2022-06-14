package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.bean.User;
import com.luohao.reggie.mapper.UserMapper;
import com.luohao.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImp extends ServiceImpl<UserMapper, User> implements UserService {
}
