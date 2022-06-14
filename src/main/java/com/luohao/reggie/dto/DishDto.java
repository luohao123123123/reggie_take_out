package com.luohao.reggie.dto;


import com.luohao.reggie.bean.Dish;
import com.luohao.reggie.bean.DishFlavor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * dto:用于展示层与服务处之间的数据传输
 *
 * DishDto：用于添加菜品时的前后端的数据传输
 * 添加菜品时不仅有Dish实体类的属性，还有DishFlavor的属性，所以需要一个dto来进行数据的传输
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DishDto extends Dish {

    //菜品口味
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    //菜品的份数
    private Integer copies;
}
