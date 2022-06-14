package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.ShoppingCart;
import com.luohao.reggie.common.BaseContext;
import com.luohao.reggie.mapper.ShoppingCartMapper;
import com.luohao.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImp extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    /**
     * 添加菜品或者套餐到购物车
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart shoppingCartAdd(ShoppingCart shoppingCart) {
        //设置购物车菜品或者套餐的用户id
        shoppingCart.setUserId(BaseContext.getId());
        //判断是否添加的是同一个套餐或者菜品
        //获取需要添加菜品的菜品id和套餐的套餐id
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        //根据菜品id去数据库查找是否已经添加过此菜品了
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getId());  //根据用户id查购物车
        queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,dishId);  //如果是菜品，则根据菜品id查购物车
        queryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,setmealId); //如果是套餐。则根据套餐id差购物车
        int count = this.count(queryWrapper); //这里获取的是购物车相同菜品或者套餐的数量
        //如果count大于，说明购物车已经添加过此菜品或者套餐了，则修改此菜品或者套餐的数量加1
        if(count>0) {
            ShoppingCart shoppingCart1 = this.getOne(queryWrapper);
            //数量加一
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            //执行更新
            this.updateById(shoppingCart1);
            return shoppingCart1;
        }
        else { //如果不满足上面的条件，说明此前没有添加过此菜品，则新增
            shoppingCart.setNumber(1);
            this.save(shoppingCart);
            return shoppingCart;
        }
    }

    /**
     * 删减菜品
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart deleteShoppingCart(ShoppingCart shoppingCart) {
        //删减之前，需要判断该删减的菜品或者套餐在购物车中的数量，如果数量大于1，则-1，如果数量等于1，则删除这个菜品或者套餐
        //根据菜品id或者套餐id查询在购物车中数量
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getId()); //添加用户id条件
        queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        queryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        //执行查询
        ShoppingCart shoppingCart1 = this.getOne(queryWrapper);
        //  获取要删减菜品或者套餐的数量
        Integer number = shoppingCart1.getNumber();


        if(number>1){ //如果数量大于1，则数量-1
            shoppingCart1.setNumber(shoppingCart1.getNumber()-1);
            //执行数量-1的更新保存
            this.updateById(shoppingCart1);
            return shoppingCart1;
        }
        else {//如果数量=1，则直接在购物车中删除该菜品或者套餐
            this.remove(queryWrapper);
            shoppingCart1.setNumber(0);
            return shoppingCart1;
        }

    }
}
