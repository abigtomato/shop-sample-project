package com.abigtomato.shop.oms.service;

import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OmsService extends IService<OrderEntity> {

    /**
     * 保存订单信息
     * @param submitVO
     * @return
     */
    OrderEntity saveOrder(OrderSubmitVO submitVO);
}
