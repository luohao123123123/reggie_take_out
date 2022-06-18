package com.luohao.reggie.bean;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private Long id;

    //名称
    @ApiModelProperty(value = "名称")
    private String name;

    //用户id
    @ApiModelProperty(value = "用户id")
    private Long userId;

    //菜品id
    @ApiModelProperty(value = "菜品id")
    private Long dishId;

    //套餐id
    @ApiModelProperty(value = "套餐id")
    private Long setmealId;

    //口味
    @ApiModelProperty(value = "口味")
    private String dishFlavor;

    //数量
    @ApiModelProperty(value = "数量")
    private Integer number;

    //金额
    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    //图片
    @ApiModelProperty(value = "图片")
    private String image;

    //更新时间，这个字段在数据库中的shopping_cart是没有的，这里加上这个字段是为了防止公共字段自动填充时没有这个字段而报错
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createTime;

    //更新时间，这个字段在数据库中的shopping_cart是没有的，这里加上这个字段是为了防止公共字段自动填充时没有这个字段而报错
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;


    //更新时间，这个字段在数据库中的shopping_cart是没有的，这里加上这个字段是为了防止公共字段自动填充时没有这个字段而报错
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
