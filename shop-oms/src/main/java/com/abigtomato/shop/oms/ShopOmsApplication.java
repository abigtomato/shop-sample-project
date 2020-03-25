package com.abigtomato.shop.oms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.abigtomato.shop.oms.mapper")
public class ShopOmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopOmsApplication.class, args);
    }
}
