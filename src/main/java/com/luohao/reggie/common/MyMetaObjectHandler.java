package com.luohao.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 * 为公共字段自动填充
 */

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入操作，自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        //自动填充createTime
        metaObject.setValue("createTime", LocalDateTime.now());
        //自动填充updateTime
        metaObject.setValue("updateTime", LocalDateTime.now());
        //自动填充createUser
        metaObject.setValue("createUser", BaseContext.getId());
        //自动填充updateUser
        metaObject.setValue("updateUser", BaseContext.getId());
        //自动填充userId
//        metaObject.setValue("userId",BaseContext.getId());

    }

    /**
     * 更新操作，自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        //自动填充updateTime
        metaObject.setValue("updateTime", LocalDateTime.now());
        //自动填充updateUser
        metaObject.setValue("updateUser", BaseContext.getId());
    }
}
