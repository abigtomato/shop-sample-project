package com.abigtomato.shop.index.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.121.100:6379");
        return Redisson.create(config);
    }
}
