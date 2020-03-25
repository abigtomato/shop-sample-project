package com.abigtomato.shop.order.service;

import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.abigtomato.shop.api.order.vo.OrderConfirmVO;

public interface OrderService {

    /**
     * 生成订单
     * @return
     */
    OrderConfirmVO confirm();

    /**
     * 提交订单
     * @param submitVO
     * @return
     */
    OrderEntity submit(OrderSubmitVO submitVO);
}
