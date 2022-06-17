package com.luohao.reggie.bean;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
/**
 * 用户信息
 */
@Data
public class User implements Serializable {

    //serialVersionUID用来作为Java对象序列化中的版本标示之用；
    //如果一个序列化类没有声明这样一个static final的产量，JVM会根据各种参数为这个类计算一个；
    // 对于同样一个类，不同版本的JDK可能会得出不同的serivalVersionUID;
    // 所以为了兼容性，一般自己加一个，至于值自己定就行，不一定是1L
    private static final long serialVersionUID = 1L;

    private Long id;


    //姓名
    private String name;


    //手机号
    private String phone;


    //性别 0 女 1 男
    private String sex;


    //身份证号
    private String idNumber;


    //头像
    private String avatar;


    //状态 0:禁用，1:正常
    private Integer status;
}
