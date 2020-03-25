package com.abigtomato.shop.wms.config;

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
     * ttl延时队列（没有对应的消费者，当里面的消息超过给定的时间，会进入死信队列）
     * @return
     */
    @Bean("WMS-TTL-QUEUE")
    public Queue ttlQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", "SHOP-ORDER-EXCHANGE");   // 设置死信交换机（可以和正常交换机通过）
        map.put("x-dead-letter-routing-key", "stock.unlock");       // 设置死信的路由主键（据此找到死信队列）
        map.put("x-message-ttl", 900000);                           // 设置消息的延迟时间（超过时间进入死信队列）
        return new Queue("WMS-TTL-QUEUE", true, false, false, map);
    }

    /**
     * ttl延时绑定器（将指定队列通过路由主键绑定到指定交换机上）
     * @return
     */
    @Bean("WMS-TTL-BINDING")
    public Binding ttlBinding() {
        // 将延时队列WMS-TTL-QUEUE通过stock.ttl绑定到SHOP-ORDER-EXCHANGE上
        return new Binding("WMS-TTL-QUEUE", Binding.DestinationType.QUEUE,
                "SHOP-ORDER-EXCHANGE", "stock.ttl", null);
    }
}
