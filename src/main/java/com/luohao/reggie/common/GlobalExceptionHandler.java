package com.luohao.reggie.common;

import com.luohao.reggie.R.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;


/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})  //指定标记了这两个注解的class会被拦截到
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 用于新增数据时，数据已经存在的异常处理
     * @param exception
     * @return
     */
    @ExceptionHandler
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){
        log.error(exception.getMessage());
        if(exception.getMessage().contains("Duplicate entry") && exception.getMessage().contains("employee")){
            String username = exception.getMessage().split(" ")[2].replace("'","").trim()   ;
            return R.error("账号["+username+"]已存在");
        }
        if(exception.getMessage().contains("Duplicate entry") && exception.getMessage().contains("category")){
            String employee = exception.getMessage().split(" ")[2].replace("'","").trim()   ;
            return R.error("分类["+employee+"]已存在");
        }
        if(exception.getMessage().contains("Duplicate entry") && exception.getMessage().contains("dish")){
            String employee = exception.getMessage().split(" ")[2].replace("'","").trim()   ;
            return R.error("菜品["+employee+"]已存在");
        }
        if(exception.getMessage().contains("Duplicate entry") && exception.getMessage().contains("setmeal")){
            String employee = exception.getMessage().split(" ")[2].replace("'","").trim()   ;
            return R.error("套餐["+employee+"]已存在");
        }
        return R.error("未知错误");
    }


    /**
     *1.用户删除分类信息时，分类信息关联了套餐或者菜品，则抛出自定义异常
     * 2.用户删除菜品信息时，如果菜品的状态为在售，则抛出自定义的异常，如果菜品包含在套餐中，也抛出自定义的异常
     * @param exception 自定义的异常信息
     * @return 返回前端异常信息
     * CustomException 自定义业务异常
     */
    @ExceptionHandler
    public R<String> exceptionHandler(CustomException exception){
        log.error(exception.getMessage());
        return R.error(exception.getMessage());
    }
}
