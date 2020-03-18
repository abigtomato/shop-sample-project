package com.abigtomato.shop.index.feign;

import com.abigtomato.shop.pms.api.ShopPmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient(value = "pms-service")
public interface ShopPmsClient extends ShopPmsApi {
}
