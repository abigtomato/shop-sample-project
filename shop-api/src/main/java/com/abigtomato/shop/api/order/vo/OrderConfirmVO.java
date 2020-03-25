package com.abigtomato.shop.api.order.vo;

import com.abigtomato.shop.api.oms.vo.OrderItemVO;
import com.abigtomato.shop.api.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVO {

    private List<MemberReceiveAddressEntity> addresses;

    private List<OrderItemVO> orderItems;

    private Integer bounds;

    private String orderToken; // 防止订单重复提交
}
