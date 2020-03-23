package com.abigtomato.shop.cart.client;

import com.abigtomato.shop.api.sms.SmsApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.SMS_SERVICE)
public interface ShopSmsClient extends SmsApi {
}
