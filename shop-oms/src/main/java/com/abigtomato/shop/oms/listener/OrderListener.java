package com.abigtomato.shop.oms.listener;

import com.abigtomato.shop.oms.mapper.OrderMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
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

    /**
     * 监听支付消息，修改订单状态并减去相应库存
     * @param orderToken
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-PAY-QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP-ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.pay"}
    ))
    public void payOrder(String orderToken) {
        // 更新订单状态为已支付待发货状态
        if (this.orderMapper.payOrder(orderToken) == 1) {
            // 更新成功则发送消息减库存
            this.amqpTemplate.convertAndSend("SHOP-ORDER-EXCHANGE", "stock.minus", orderToken);
        }
    }
}
