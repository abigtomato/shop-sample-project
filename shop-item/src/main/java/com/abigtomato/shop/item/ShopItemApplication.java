package com.abigtomato.shop.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ShopItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringApplication.class, args);
    }
}
