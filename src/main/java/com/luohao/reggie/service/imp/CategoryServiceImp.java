package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.bean.Category;
import com.luohao.reggie.bean.Dish;
import com.luohao.reggie.bean.Setmeal;
import com.luohao.reggie.common.CustomException;
import com.luohao.reggie.mapper.CategoryMapper;
import com.luohao.reggie.service.CategoryService;
import com.luohao.reggie.service.DishService;
import com.luohao.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImp extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;  //菜品Service

    @Autowired
    private SetmealService setmealService; //套餐Service


    /**
     * 根据分类id删除分类信息
     * 删除之前需要进行判断这个分类是否已经关联了菜品或者套餐
     * @param id 分类id
     */
    @Override
    public void remove(Long id) {
        //查询当前分类是否已经关联了菜品，如果已经关联，抛出一个异常
        LambdaQueryWrapper<Dish> dishQueryWrapper=new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId,id);  //添加查询条件，菜品表的categoryId要等于分类表的id
        long dishCount = dishService.count(dishQueryWrapper);  //执行查询
        if(dishCount>0){ //如果大于0，说明这个菜品分类关联了菜品，则抛出业务异常
            throw new CustomException("当前分类关联了菜品，无法删除"); //抛出自定义的业务异常
        }

        //查询当前分类是否已经关联了套餐，如果已经关联，抛出一个异常
        LambdaQueryWrapper<Setmeal> setMealQueryWrapper=new LambdaQueryWrapper<>();
        setMealQueryWrapper.eq(Setmeal::getCategoryId,id);  //添加查询条件，菜品表的categoryId要等于分类表的id
        long setMealCount = setmealService.count(setMealQueryWrapper);  //执行查询
        if(setMealCount>0){ //如果大于0，说明这个套餐分类关联了套餐，则抛出业务异常
            throw new CustomException("当前分类关联了套餐，无法删除"); //抛出自定义的业务异常
        }

        //正常删除
         super.removeById(id);
    }
}
