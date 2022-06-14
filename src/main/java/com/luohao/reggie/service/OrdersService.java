package com.luohao.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.Orders;
import com.luohao.reggie.dto.OrdersDto;

public interface OrdersService extends IService<Orders> {


    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);


    /**
     * 用于用户的分页查询订单  ,bug：用户查看历史订单，无法看到订单中有多少菜品
     * 1.先查询出订单基本信息的分页数据
     * 2.在构造OrderDto的分页对象
     * 3.进行Page<order>和Page<OrderDto>的数据拷贝
     * 4.需要根据查询出的订单信息，去再次查询订单明细信息，然后封装成OrderDto对象
     * 5.最后返回Page<OrderDto>
     * @return Page<OrderDto>
     */
    public Page<OrdersDto> userOrderPageWithOrderDetail(int page, int pageSize);
}
