package com.abigtomato.shop.wms.api;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.wms.entity.WareSkuEntity;
import com.abigtomato.shop.wms.vo.SkuLockVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ShopWmsApi {

    @GetMapping("/wms/waresku/{skuId}")
    Resp<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId") Long skuId);

    @PostMapping("/wms/waresku")
    Resp<Object> checkAndLockStore(@RequestBody List<SkuLockVo> skuLockVOS);
}
