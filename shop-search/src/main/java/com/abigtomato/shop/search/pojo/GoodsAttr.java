package com.abigtomato.shop.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 规格字段
 */
@Data
public class GoodsAttr {

    @Field(type = FieldType.Long)
    private Long attrId;    // 规格id

    @Field(type = FieldType.Keyword)
    private String attrName;    // 规格名称

    @Field(type = FieldType.Keyword)
    private String attrValue;   // 规格名称对应的值
}
