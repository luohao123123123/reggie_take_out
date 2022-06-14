package com.luohao.reggie.bean;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;

    /**
     * 公共字段填充：
     * 在实体类的属性上加上@TableField注解，并且指定填充策略
     * 然后编写元数据对象处理器，在此类中统一为公共字段赋值，此类需要实现MetaObjectHandler接口
     *
     * MyMetaObjectHandler
     */
    @TableField(fill = FieldFill.INSERT)   //插入时自动填充字段
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)  //插入和更新时自动填充字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)  //插入时自动填充字段
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)  //插入和更新时自动填充字段
    private Long updateUser;

}
