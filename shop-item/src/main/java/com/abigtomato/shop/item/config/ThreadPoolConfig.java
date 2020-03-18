package com.abigtomato.shop.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        // 池中线程数量为50，最大可扩展线程数为500，时间为30秒，阻塞队列容量10000
        return new ThreadPoolExecutor(50, 500, 30,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
    }
}
