package com.luohao.reggie.dto;



import com.luohao.reggie.bean.OrderDetail;
import com.luohao.reggie.bean.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
