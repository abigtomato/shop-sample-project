package com.abigtomato.shop.order.client;

import com.abigtomato.shop.api.cart.CartApi;
import com.abigtomato.shop.core.client.ServiceNameList;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = ServiceNameList.CART_SERVICE)
public interface ShopCartClient extends CartApi {
}
