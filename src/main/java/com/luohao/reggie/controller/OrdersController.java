package com.luohao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.OrderDetail;
import com.luohao.reggie.bean.Orders;
import com.luohao.reggie.common.BaseContext;
import com.luohao.reggie.dto.DishDto;
import com.luohao.reggie.dto.OrdersDto;
import com.luohao.reggie.service.OrderDetailService;
import com.luohao.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;


    /**
     * 用户提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        //调用自定义的用户下单方法
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用于用户的分页查询订单  ,bug：用户查看历史订单，无法看到订单中有多少菜品
     * 1.先查询出订单基本信息的分页数据
     * 2.在构造OrderDto的分页对象
     * 3.进行Page<order>和Page<OrderDto>的数据拷贝
     * 4.需要根据查询出的订单信息，去再次查询订单明细信息，然后封装成OrderDto对象
     * 5.最后返回Page<OrderDto>
     * @return Page<OrderDto>
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize){
        //调用自定义的分页方法
        Page<OrdersDto> ordersDtoPage = ordersService.userOrderPageWithOrderDetail(page, pageSize);
        //返回dishDtoPage
        return R.success(ordersDtoPage);

    }

    /**
     * 用户再来一单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        return R.success("再来一单成功");
    }


    /**
     * 用于后台的订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(int page,int pageSize,String number,String beginTime,String endTime){
        Page<Orders> pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(number!=null,Orders::getNumber,number); //根据订单号查找
        queryWrapper.orderByDesc(Orders::getOrderTime);//根据订单创建时间排序
//        LocalDateTime begin = LocalDateTime.parse(beginTime);
//        LocalDateTime end = LocalDateTime.parse(endTime);
        queryWrapper.between(beginTime!=null && endTime!=null,Orders::getOrderTime,beginTime,endTime);
        ordersService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    // 取消，派送，完成接口
    @PutMapping
    public R<String> editOrderDetail(@RequestBody Orders orders){
        //根据订单的id修改订单的状态
        Orders orders1 = ordersService.getById(orders.getId());
        //修改订单的状态
        orders1.setStatus(orders.getStatus());
        //执行更新
        ordersService.updateById(orders);
        return R.success("更新订单状态完成");

    }
}
