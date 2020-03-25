package com.abigtomato.shop.oms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * rabbitmq配置类
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 配置超过关单的延时队列
     * @return
     */
    @Bean("ORDER-TTL-QUEUE")
    public Queue ttlQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", "SHOP-ORDER-EXCHANGE");   // 死信交换机
        map.put("x-dead-letter-routing-key", "order.dead");         // 死信路由
        map.put("x-message-ttl", 1200000);                          // 超时时间
        return new Queue("ORDER-TTL-QUEUE", true, false, false, map);
    }

    /**
     * 绑定超时关单相关的交换机，路由和延时队列
     * @return
     */
    @Bean("ORDER-TTL-BINDING")
    public Binding ttlBinding() {
        return new Binding("ORDER-TTL-QUEUE", Binding.DestinationType.QUEUE,
                "SHOP-ORDER-EXCHANGE", "order.ttl", null);
    }

    /**
     * 配置超时关单的死信队列
     * @return
     */
    @Bean("ORDER-DEAD-QUEUE")
    public Queue dlQueue() {
        return new Queue("ORDER-DEAD-QUEUE", true, false, false, null);
    }

    /**
     * 绑定超时关单相关的交换机，路由和死信队列
     * @return
     */
    @Bean("ORDER-DEAD-BINDING")
    public Binding deadBinding() {
        return new Binding("ORDER-DEAD-QUEUE", Binding.DestinationType.QUEUE,
                "SHOP-ORDER-EXCHANGE", "order.dead", null);
    }
}
