package com.abigtomato.shop.order.controller;

import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.abigtomato.shop.api.order.OrderApi;
import com.abigtomato.shop.api.order.vo.OrderConfirmVO;
import com.abigtomato.shop.api.order.vo.PayAsyncVo;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
@Slf4j
public class OrderController implements OrderApi {

    private OrderService orderService;

    private AmqpTemplate amqpTemplate;

    private StringRedisTemplate redisTemplate;

    private RedissonClient redissonClient;

    @Autowired
    public OrderController(OrderService orderService,
                           AmqpTemplate amqpTemplate,
                           StringRedisTemplate redisTemplate,
                           RedissonClient redissonClient) {
        this.orderService = orderService;
        this.amqpTemplate = amqpTemplate;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    @Override
    public Resp<OrderConfirmVO> confirm() {
        return Resp.ok(this.orderService.confirm());
    }

    @Override
    public Resp<OrderEntity> submit(@RequestBody OrderSubmitVO submitVO) {
        return Resp.ok(this.orderService.submit(submitVO));
    }

    @Override
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo) {
        return null;
    }

    @Override
    public Resp<Object> seckill(Long skuId) {
        return null;
    }

    @Override
    public Resp<Object> querySeckill(String orderToken) throws InterruptedException {
        return null;
    }
}
