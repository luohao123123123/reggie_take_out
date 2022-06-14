package com.luohao.reggie.common;

/**
 * 基于ThreadLocal封装工具类
 * 用于保存和获取当前登录用户的id
 */
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    //保存后台id
    public static void setId(Long id){
        threadLocal.set(id);
    }

    //获取用户id
    public static Long getId(){
        return threadLocal.get();
    }


}
