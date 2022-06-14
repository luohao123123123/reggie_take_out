package com.luohao.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luohao.reggie.bean.Setmeal;
import com.luohao.reggie.dto.DishDto;
import com.luohao.reggie.dto.SetmealDto;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 用于套餐管理页面的分页
     * 由于前端页面需要分类name，而套餐中只有分类id，所有需要根据分类id获取分类name
     * 由于setmael中没有分类name这个字段，所有要封装成setmealDto后，传给前端
     * @param page 第几页
     * @param pageSize 一页多少条数据
     * @param name  套餐name
     * @return
     */
    public Page<SetmealDto> dtoPageWithCategoryName(int page,int pageSize,String name);

    /**
     * 用于套餐修改页面的回显
     * @param id 套餐id
     * @return
     */
    public SetmealDto get(Long id);


    /**
     * 用于套餐页面的套餐修改
     * @param setmealDto
     */
    public void updateSetmealAndDish(SetmealDto setmealDto);


    /**
     * 用于套餐页面的套餐删除
     * @param ids
     */
    public void deleteWithDish(List<Long> ids);

    /**
     * 用于套餐页面的套餐状态修改
     * @param status
     * @param ids
     */
    public void updateWithStatus(Integer status,List<Long> ids);


    /**
     * 用于移动端点击套餐查看套餐中的菜品信息
     * @param setmealId
     * @return
     */
    public List<DishDto> getSetmealWithDish(Long setmealId);
}
