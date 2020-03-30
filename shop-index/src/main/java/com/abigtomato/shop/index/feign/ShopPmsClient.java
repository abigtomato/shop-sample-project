package com.abigtomato.shop.index.feign;

import com.abigtomato.shop.api.pms.PmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@FeignClient(value = "pms-service")
public interface ShopPmsClient extends PmsApi {
}
