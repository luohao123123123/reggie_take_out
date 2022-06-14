package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.bean.OrderDetail;
import com.luohao.reggie.mapper.OrderDetailMapper;
import com.luohao.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImp extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
