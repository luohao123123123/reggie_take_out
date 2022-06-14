package com.luohao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luohao.reggie.bean.Category;

public interface CategoryService extends IService<Category> {
    //根据分类id删除分类信息
    public void remove(Long id);
}
