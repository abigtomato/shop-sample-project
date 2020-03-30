package com.abigtomato.shop.item.feign;

import com.abigtomato.shop.api.sms.SmsApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.SMS_SERVICE)
public interface ShopSmsClient extends SmsApi {
}
