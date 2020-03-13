package com.abigtomato.shop.sms.api;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ShopSmsApi {

    @PostMapping("sms/skubounds/sku/sale/save")
    Resp<Object> saveSale(@RequestBody SkuSaleVo skuSaleVO);
}
