package com.abigtomato.shop.api.cart;

import com.abigtomato.shop.api.cart.model.Cart;
import com.abigtomato.shop.core.bean.Resp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "购物车接口")
public interface CartApi {

    @PostMapping
    @ApiOperation(value = "添加购物车")
    Resp<Void> addCart(@RequestBody Cart cart);

    @GetMapping
    @ApiOperation(value = "查询购物车列表")
    Resp<List<Cart>> queryCarts();

    @PostMapping("/update")
    @ApiOperation(value = "更新购物车")
    Resp<Void> updateCart(@RequestBody Cart cart);

    @GetMapping("/{userId}")
    @ApiOperation(value = "根据userId查询购物车")
    Resp<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId") String userId);

    @DeleteMapping("/{skuId}")
    @ApiOperation(value = "根据skuId删除购物车")
    Resp<Void> deleteCart(@PathVariable("skuId") Long skuId);
}
