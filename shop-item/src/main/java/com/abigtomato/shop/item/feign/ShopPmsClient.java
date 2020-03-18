package com.abigtomato.shop.item.feign;

import com.abigtomato.shop.pms.api.ShopPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "wms-service")
public interface ShopPmsClient extends ShopPmsApi {
}
