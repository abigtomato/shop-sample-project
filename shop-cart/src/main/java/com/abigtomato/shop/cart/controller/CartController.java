package com.abigtomato.shop.cart.controller;

import com.abigtomato.shop.api.cart.CartApi;
import com.abigtomato.shop.api.cart.model.Cart;
import com.abigtomato.shop.cart.service.CartService;
import com.abigtomato.shop.core.bean.Resp;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@Slf4j
public class CartController implements CartApi {

    private CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    @PostMapping
    public Resp<Void> addCart(@RequestBody Cart cart) {
        this.cartService.addCart(cart);
        return Resp.ok(null);
    }

    @Override
    @GetMapping
    public Resp<List<Cart>> queryCarts() {
        return Resp.ok(this.cartService.queryCarts());
    }

    @Override
    @PostMapping("/update")
    public Resp<Void> updateCart(@RequestBody Cart cart) {
        this.cartService.updateCart(cart);
        return Resp.ok(null);
    }

    @Override
    public Resp<List<Cart>> queryCheckedCartsByUserId(String userId) {
        return null;
    }

    @GetMapping("/{userId}")
    public Resp<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId") Long userId) {
        return Resp.ok(this.cartService.queryCheckedCartsByUserId(userId));
    }

    @Override
    @DeleteMapping("/{skuId}")
    public Resp<Void> deleteCart(@PathVariable("skuId") Long skuId) {
        this.cartService.deleteCart(skuId);
        return Resp.ok(null);
    }
}
