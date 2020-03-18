package com.abigtomato.shop.pms.vo;

import com.abigtomato.shop.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

@Data
public class ItemGroupVo {

    private String name;

    private List<ProductAttrValueEntity> baseAttrs;
}
