package com.abigtomato.shop.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * 商品document
 */
@Data
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
public class GoodsDoc {

    @Id
    private Long skuId; // 商品sku的id

    @Field(type = FieldType.Keyword, index = false)
    private String pic; // 主图

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;   // 标题

    @Field(type = FieldType.Double)
    private Double price;   // 价格

    @Field(type = FieldType.Long)
    private Long sale;  // 销量

    @Field(type = FieldType.Boolean)
    private Boolean store;  // 是否有货

    @Field(type = FieldType.Date)
    private Date createTime;    // 创建时间（新品排序）

    @Field(type = FieldType.Long)
    private Long brandId;   // 品牌id

    @Field(type = FieldType.Keyword)
    private String brandName;   // 品牌名称

    @Field(type = FieldType.Long)
    private Long categoryId;    // 分类id

    @Field(type = FieldType.Keyword)
    private String categoryName;    // 分类名称

    @Field(type = FieldType.Nested)
    private List<GoodsAttr> attrs; // 规格属性（嵌套字段）
}
