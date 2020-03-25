package com.abigtomato.shop.api.oms;

import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.abigtomato.shop.core.bean.Resp;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface OmsApi {

    @PostMapping("oms/order")
    Resp<OrderEntity> saveOrder(@RequestBody OrderSubmitVO submitVO);
}
