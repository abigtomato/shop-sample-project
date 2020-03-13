package com.abigtomato.shop.pms.vo;

import com.abigtomato.shop.pms.entity.SpuInfoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpuInfoVo extends SpuInfoEntity {

    private List<String> spuImages;

    private List<BaseAttrVo> baseAttrs;

    private List<SkuInfoVo> skuInfos;
}
