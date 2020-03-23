package com.abigtomato.shop.api.pms.vo;

import com.abigtomato.shop.api.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

@Data
public class ItemGroupVO {

    private String name;

    private List<ProductAttrValueEntity> baseAttrs;
}
