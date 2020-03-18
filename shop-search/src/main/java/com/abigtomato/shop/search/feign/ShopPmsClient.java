package com.abigtomato.shop.search.feign;

import com.abigtomato.shop.pms.api.ShopPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "pms-service")
public interface ShopPmsClient extends ShopPmsApi {
}
