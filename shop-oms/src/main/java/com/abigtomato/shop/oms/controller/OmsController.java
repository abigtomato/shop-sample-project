package com.abigtomato.shop.oms.controller;

import com.abigtomato.shop.api.oms.OmsApi;
import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.oms.service.OmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("oms/order")
@Slf4j
public class OmsController implements OmsApi {

    private OmsService omsService;

    @Autowired
    public OmsController(OmsService omsService) {
        this.omsService = omsService;
    }

    @Override
    @PostMapping("oms/order")
    public Resp<OrderEntity> saveOrder(@RequestBody OrderSubmitVO submitVO) {
        return Resp.ok(this.omsService.saveOrder(submitVO));
    }
}
