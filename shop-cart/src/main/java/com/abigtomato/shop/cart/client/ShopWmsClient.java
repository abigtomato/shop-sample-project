package com.abigtomato.shop.cart.client;

import com.abigtomato.shop.api.wms.WmsApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.WMS_SERVICE)
public interface ShopWmsClient extends WmsApi {
}
