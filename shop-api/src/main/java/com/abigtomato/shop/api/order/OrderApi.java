package com.abigtomato.shop.api.order;

import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.abigtomato.shop.api.order.vo.OrderConfirmVO;
import com.abigtomato.shop.api.order.vo.PayAsyncVo;
import com.abigtomato.shop.core.bean.Resp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface OrderApi {

    @GetMapping("confirm")
    Resp<OrderConfirmVO> confirm();

    @PostMapping("submit")
    Resp<OrderEntity> submit(@RequestBody OrderSubmitVO submitVO);

    @PostMapping("pay/success")
    Resp<Object> paySuccess(@RequestBody PayAsyncVo payAsyncVo);

    @PostMapping("seckill/{skuId}")
    Resp<Object> seckill(@PathVariable("skuId") Long skuId);

    @GetMapping("seckill/{orderToken}")
    Resp<Object> querySeckill(@PathVariable("orderToken") String orderToken) throws InterruptedException;
}
