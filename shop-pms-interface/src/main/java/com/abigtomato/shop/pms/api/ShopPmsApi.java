package com.abigtomato.shop.pms.api;

import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.pms.entity.*;
import com.abigtomato.shop.pms.vo.CategoryVo;
import com.abigtomato.shop.pms.vo.ItemGroupVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ShopPmsApi {

    @PostMapping("pms/spuinfo/page")
    Resp<List<SpuInfoEntity>> querySpusByPage(@RequestBody QueryCondition queryCondition);

    @GetMapping("pms/spuinfo/info/{id}")
    Resp<SpuInfoEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/skuinfo/{spuId}")
    Resp<List<SkuInfoEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skuinfo/info/{skuId}")
    Resp<SkuInfoEntity> querySkuById(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/skuimages/{skuId}")
    Resp<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/brand/info/{brandId}")
    Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    @GetMapping("pms/category")
    Resp<List<CategoryEntity>> queryCategoriesByPidOrLevel(@RequestParam(value = "level", defaultValue = "0") Integer level);

    @GetMapping("pms/category/{pid}")
    Resp<List<CategoryVo>> querySubCategories(@PathVariable("pid") Long pid);

    @GetMapping("pms/productattrvalue/{spuId}")
    Resp<List<ProductAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skusaleattrvalue/{spuId}")
    Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValuesBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skusaleattrvalue/sku/{skuId}")
    Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValuesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/attrgroup/item/group/{cid}/{spuId}")
    Resp<List<ItemGroupVo>> queryItemGroupVOByCidAndSpuId(@PathVariable("cid") Long cid, @PathVariable("spuId") Long spuId);

    @GetMapping("pms/spuinfodesc/info/{spuId}")
    Resp<SpuInfoDescEntity> querySpuDescBySpuId(@PathVariable("spuId") Long spuId);
}
