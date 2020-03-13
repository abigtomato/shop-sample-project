package com.abigtomato.shop.pms.feign;

import com.abigtomato.shop.sms.api.ShopSmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient(value = "sms-service")
public interface ShopSmsClient extends ShopSmsApi {
}
