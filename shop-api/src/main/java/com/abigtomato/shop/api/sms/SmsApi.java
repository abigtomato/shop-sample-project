package com.abigtomato.shop.api.sms;

import com.abigtomato.shop.api.sms.vo.SaleVO;
import com.abigtomato.shop.api.sms.vo.SkuSaleVO;
import com.abigtomato.shop.core.bean.Resp;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Api(tags = "营销管理api")
public interface SmsApi {

    @PostMapping("sms/skubounds/sku/sale/save")
    Resp<Object> saveSale(@RequestBody SkuSaleVO skuSaleVO);

    @GetMapping("sms/skubounds/{skuId}")
    Resp<List<SaleVO>> querySalesBySkuId(@PathVariable("skuId") Long skuId);
}
