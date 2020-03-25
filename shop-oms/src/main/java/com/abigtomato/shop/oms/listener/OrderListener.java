package com.abigtomato.shop.oms.listener;

import com.abigtomato.shop.oms.mapper.OrderMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    private OrderMapper orderMapper;

    private AmqpTemplate amqpTemplate;

    public OrderListener(OrderMapper orderMapper,
                         AmqpTemplate amqpTemplate) {
        this.orderMapper = orderMapper;
        this.amqpTemplate = amqpTemplate;
    }

    /**
     * 监听死信队列，超时关单
     * @param orderToken
     */
    @RabbitListener(queues = {"ORDER-DEAD-QUEUE"})
    public void closeOrder(String orderToken) {
        if (this.orderMapper.closeOrder(orderToken) == 1) {
            // 关单成功后解锁库存
            this.amqpTemplate.convertAndSend("SHOP-ORDER-EXCHANGE", "stock.unlock", orderToken);
        }
    }
}
