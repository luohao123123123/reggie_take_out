package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.*;
import com.luohao.reggie.common.BaseContext;
import com.luohao.reggie.common.CustomException;
import com.luohao.reggie.dto.OrdersDto;
import com.luohao.reggie.mapper.OrdersMapper;
import com.luohao.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrdersServiceImp extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;


    /**
     * 用户下单
     * @param orders
     */
    @Transactional
    @Override
    public void submit(Orders orders) {
        //获取当前用户
        Long userId = BaseContext.getId();


        //查询用户信息
        User user = userService.getById(userId);


        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        //如果当前用户的购物车没有数据，则无法下单
        if(shoppingCartList.size()==0){
            throw new CustomException("购物车没有菜品或套餐，请先添加");
        }

        //查询地址数据
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook==null){
            throw new CustomException("用户地址信息有误，无法下单");
        }

        //设置订单的信息
             //设置订单的用户
        orders.setUserId(userId);
            //设置订单的状态，1待付款，2待派送，3已派送，4已完成，5已取消（这里是第一次提交，所以是待付款，也可以不用设置，因为数据库中默认为1）
        orders.setStatus(1);
            //设置下单时间
        orders.setOrderTime(LocalDateTime.now());
            //设置结账时间
        orders.setCheckoutTime(LocalDateTime.now());
            //设置订单号
        orders.setNumber(IdWorker.get32UUID());
            //设置用户名
        orders.setUserName(user.getName());
            //设置收货人
        orders.setConsignee(addressBook.getConsignee());
            //设置手机号
        orders.setPhone(addressBook.getPhone());
            //设置地址
        orders.setAddress(
                (addressBook.getProvinceName()==null?"":addressBook.getProvinceName())
                + (addressBook.getCityName()==null?"":addressBook.getCityName())
                + (addressBook.getDistrictName()==null?"":addressBook.getDistrictName())
                + (addressBook.getDetail()==null?"":addressBook.getDetail())


        );
        System.out.println("用户地址："+orders.getAddress());
            //设置实收金额
        AtomicInteger amount=new AtomicInteger(0);  //原子整形，可以保证多线程下的金额计算不出错

        for(ShoppingCart shoppingCart:shoppingCartList){
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());  //菜品价格乘以菜品份数
        }
        orders.setAmount(new BigDecimal(amount.get()));


        //向订单表插入数据
        this.save(orders);

        //向订单明细表插入数据
            //购物车菜品和订单明细进行数据拷贝
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(x -> {
            //创建订单明细对象
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(x, orderDetail);
            //设置订单明细表的订单id
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());
            //保存订单明细
        orderDetailService.saveBatch(orderDetailList);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);


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
    @Transactional
    @Override
    public Page<OrdersDto> userOrderPageWithOrderDetail(int page, int pageSize) {
        //todo:查询出用户的所有订单信息
        //构建分页构造器
        Page<Orders> pageInfo=new Page<>(page,pageSize);
        //构建条件构造器
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        //根据userid查询每个user的order
        queryWrapper.eq(Orders::getUserId,BaseContext.getId());
        //根据订单创建时间降序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //执行分页
        this.page(pageInfo,queryWrapper);

        //todo，查询出用户所有订单的订单明细信息，封装成OrderDto对象
        //创建Page<OrderDto>对象
        Page<OrdersDto> dishDtoPage=new Page<>();
        //进行数据的拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");


        //获取分页信息中的所有的订单信息
        List<Orders> ordersList = pageInfo.getRecords();


        //进行数据order和orderDto的数据拷贝
        List<OrdersDto> ordersDtoList = ordersList.stream().map(x -> {
            //创建OrderDto对象
            OrdersDto ordersDto = new OrdersDto();
            //进行数据的拷贝
            BeanUtils.copyProperties(x, ordersDto);
            //获取订单的id
            Long orderId = x.getId();
            //根据订单id查询订单明细信息
            LambdaQueryWrapper<OrderDetail> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId,orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper1);
            //把查询到的订单的所有明细封装给orderDto
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());
        //把ordersDtoList封装进行dishDtoPage
        dishDtoPage.setRecords(ordersDtoList);
        //返回Page<OrderDto>
        return dishDtoPage;
    }
}
