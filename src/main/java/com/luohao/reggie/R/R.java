package com.luohao.reggie.R;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果
 * @param <T>
 */
@Api(tags = "统一返回结果")
@Data
public class R<T> implements Serializable {

    @ApiModelProperty(value = "状态码")
    private Integer code; //编码：1成功，0和其它数字为失败

    @ApiModelProperty(value = "返回信息")
    private String msg; //错误信息

    @ApiModelProperty(value = "返回数据")
    private T data; //数据

    @ApiModelProperty(value = "其他动态数据")
    private Map<Object,Object> map = new HashMap<>(); //动态数据

    @ApiOperation(value = "成功结果")
    public static <T> R<T> success(T object) {
        R<T> r = new R<>();
        r.data = object;
        r.code = 1;
        return r;
    }

    @ApiOperation(value = "失败结果")
    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
