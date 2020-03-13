package com.abigtomato.shop.pms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableSwagger2
@MapperScan(value = "com.abigtomato.shop.pms.mapper")
public class ShopPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopPmsApplication.class, args);
    }
}
