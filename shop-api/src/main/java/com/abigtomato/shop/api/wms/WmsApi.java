package com.abigtomato.shop.api.wms;

import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.abigtomato.shop.api.wms.vo.SkuLockVO;
import com.abigtomato.shop.core.bean.Resp;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Api(tags = "库存管理api")
public interface WmsApi {

    @GetMapping("wms/waresku/{skuId}")
    Resp<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId") Long skuId);

    @PostMapping("wms/waresku")
    Resp<Object> checkAndLockStore(@RequestBody List<SkuLockVO> skuLockVOS);
}
