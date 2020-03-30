package com.abigtomato.shop.sms.controller;

import com.abigtomato.shop.api.sms.vo.SkuSaleVO;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.sms.service.SkuBoundsService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "商品sku积分设置管理")
@RestController
@RequestMapping("sms/skubounds")
@Slf4j
public class SkuBoundsController {

    private SkuBoundsService skuBoundsService;

    @Autowired
    public SkuBoundsController(SkuBoundsService skuBoundsService) {
        this.skuBoundsService = skuBoundsService;
    }

    @PostMapping("sku/sale/save")
    public Resp<Object> saveSale(@RequestBody SkuSaleVO skuSaleVo) {
        this.skuBoundsService.saveSale(skuSaleVo);
        return Resp.ok(null);
    }
}
