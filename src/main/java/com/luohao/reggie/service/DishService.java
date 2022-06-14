package com.luohao.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.Dish;
import com.luohao.reggie.dto.DishDto;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 用于新增菜品时，进行数据保存，需要保存菜品的基本信息，同时保存菜品的口味，
     * 所以需要同时操作Dish和DishFlavor表
     * @param dishDto
     */
    public void saveWithFlavor(DishDto dishDto);

    /**
     * 用于菜品管理页面的分页查询，同时需要根据菜品id查询对应的菜品分类的name，返回给前端
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    public Page<DishDto> dtoPageWithCategoryName(int page, int pageSize, String name);


    /**
     * 用于修改菜品信息时的菜品信息回显
     * 这里需要回显菜品的基本信息，同时需要回显菜品的口味信息
     * @param id 菜品id
     * @return
     */
    public DishDto getDishWithFlavor(Long id);


    /**
     * 用于保存修改后的菜品基本信息和菜品口味信息
     * @param dishDto
     */
    public void updateDishWithFlavor(DishDto dishDto);


    /**
     * 用于菜品的单个删除和批量删除
     * @param ids
     */
    public R<String> delete(List<Long> ids);
}
