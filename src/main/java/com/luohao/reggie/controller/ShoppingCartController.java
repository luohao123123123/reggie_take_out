package com.luohao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.ShoppingCart;
import com.luohao.reggie.common.BaseContext;
import com.luohao.reggie.common.CustomException;
import com.luohao.reggie.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "购物车Controller")
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    ShoppingCartService shoppingCartService;

    /**
     * 获取用户的购物车内商品的集合
     * @return
     */
    @ApiOperation(value = "获取用户购物车的全部商品信息")
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        //根据用户id获取用户的购物车内的商品集合
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCartList);
    }

    /**
     * 用于新增菜品或者套餐到购物车
     * @param shoppingCart
     * @return
     */
    @ApiOperation(value = "新增菜品或者套餐到购物车")
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //执行自定义的添加菜品或者套餐到购物车
        ShoppingCart shoppingCart1 = shoppingCartService.shoppingCartAdd(shoppingCart);

        return R.success(shoppingCart1);
    }

    /**
     * 购物车菜品的删减
     * @param shoppingCart
     * @return
     */
    @ApiOperation(value = "购物车菜品的删减")
    @PostMapping("/sub")
    public R<ShoppingCart> deleteShoppingCart(@RequestBody ShoppingCart shoppingCart){
        ShoppingCart shoppingCart1 = shoppingCartService.deleteShoppingCart(shoppingCart);
        return R.success(shoppingCart1);
    }

    /**
     * 清空购物车
     * @return
     */
    @ApiOperation(value = "清空购物车")
    @DeleteMapping("/clean")
    public R<String> cleanShoppingCart(){
        //根据用户id，清空购物车
        //获取当前用户id
        Long userId = BaseContext.getId();
        //构造条件构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        //执行删除
        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");

    }
}
