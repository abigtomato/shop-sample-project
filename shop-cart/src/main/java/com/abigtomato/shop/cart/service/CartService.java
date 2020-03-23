package com.abigtomato.shop.cart.service;

import com.abigtomato.shop.api.cart.model.Cart;

import java.util.List;

public interface CartService {

    void addCart(Cart cart);

    List<Cart> queryCarts();

    void updateCart(Cart cart);

    List<Cart> queryCheckedCartsByUserId(Long userId);

    void deleteCart(Long skuId);
}
