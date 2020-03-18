package com.abigtomato.shop.item.controller;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.item.service.ItemService;
import com.abigtomato.shop.item.vo.ItemVo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/item")
@Slf4j
@Api(tags = "详情管理")
public class ItemController {

    private ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping(value = "/{skuId}")
    public Resp<ItemVo> queryItemVo(@PathVariable("skuId") Long skuId) {
        ItemVo itemVO = this.itemService.queryItemVo(skuId);
        return Resp.ok(itemVO);
    }
}
