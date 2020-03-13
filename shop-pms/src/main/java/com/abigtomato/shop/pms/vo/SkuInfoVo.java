package com.abigtomato.shop.pms.vo;

import com.abigtomato.shop.pms.entity.SkuInfoEntity;
import com.abigtomato.shop.pms.entity.SkuSaleAttrValueEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SkuInfoVo extends SkuInfoEntity {

    // 积分营销相关字段
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    // 打折相关的字段
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    // 满减相关的字段
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    // 销售属性及值
    private List<SkuSaleAttrValueEntity> saleAttrs;
    // sku图片
    private List<String> images;
}
