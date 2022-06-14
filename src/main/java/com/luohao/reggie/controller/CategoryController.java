package com.luohao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.Category;
import com.luohao.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 分类管理
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;


    /**
     * 新增分类
     * @param category 分类
     * @return
     */
    @PostMapping
    public R<Integer> add(@RequestBody Category category){
        categoryService.save(category);

        //1为菜品，2为套餐
        String type = category.getType() == 1 ? "菜品" : "套餐";
        log.info("新增{}分类成功",type);
        return R.success(category.getType());

    }

    /**
     * 分类信息分页查询
     * @param page 第几页
     * @param pageSize 一页多少条
     * @return
     */
    @GetMapping("/page")
    public R<Page<Category>> page(int page,int pageSize){
        //构造分页构造器
        Page<Category> pageInfo=new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
            //根据sort字段升序
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据分类id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id){
        //在删除之前，要判断这个分类信息有没有关联菜品或者套餐
        //如果关联了菜品或者套餐，就抛出业务异常，无法进行删除
        categoryService.remove(id);   //这里调用自定义的删除方法，在CategoryServiceImp实现了业务逻辑
        return R.success("分类删除成功");
    }


    /**
     * 分类信息的修改
     * @param category
     * @return
     */
    @PutMapping
    public R<Integer> update(@RequestBody Category category){
        //执行更新操作
        categoryService.updateById(category);
        //获取分类信息的type
        Integer categoryType = categoryService.getById(category).getType();
        return R.success(categoryType);
    }


    /**
     * 菜品管理中的添加菜品，需要获取到所有的菜品分类
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //构造条件查询器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        //添加条件type
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime);
        //执行查询
        List<Category> categoryList = categoryService.list(queryWrapper);
        //把获取到的菜品分类返回给前端，用于添加菜品时进行选择菜品分类
        return R.success(categoryList);
    }
}
