package com.abigtomato.shop.item.feign;

import com.abigtomato.shop.api.wms.WmsApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.WMS_SERVICE)
public interface ShopWmsClient extends WmsApi {
}
