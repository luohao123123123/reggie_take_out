package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.bean.DishFlavor;
import com.luohao.reggie.mapper.DishFlavorMapper;
import com.luohao.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;


@Service
public class DishFlavorServiceImp extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
