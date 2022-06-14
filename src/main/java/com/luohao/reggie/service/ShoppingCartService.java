package com.luohao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luohao.reggie.bean.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    /**
     * 添加菜品或者套餐到购物车
     * @param shoppingCart
     * @return
     */
    public ShoppingCart shoppingCartAdd(ShoppingCart shoppingCart);

    /**
     * 删减菜品
     * @param shoppingCart
     * @return
     */
    public ShoppingCart deleteShoppingCart(ShoppingCart shoppingCart);
}
