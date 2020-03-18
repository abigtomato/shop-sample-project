package com.abigtomato.shop.item.feign;

import com.abigtomato.shop.sms.api.ShopSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "sms-service")
public interface ShopSmsClient extends ShopSmsApi {
}
