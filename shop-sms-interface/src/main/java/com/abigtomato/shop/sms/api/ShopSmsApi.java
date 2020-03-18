package com.abigtomato.shop.sms.api;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.sms.vo.SaleVo;
import com.abigtomato.shop.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ShopSmsApi {

    @PostMapping("sms/skubounds/sku/sale/save")
    Resp<Object> saveSale(@RequestBody SkuSaleVo skuSaleVO);

    @GetMapping("sms/skubounds/{skuId}")
    Resp<List<SaleVo>> querySalesBySkuId(@PathVariable("skuId") Long skuId);
}
