package com.abigtomato.shop.pms.feign;

import com.abigtomato.shop.api.sms.SmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@FeignClient(value = "sms-service")
public interface ShopSmsClient extends SmsApi {
}
