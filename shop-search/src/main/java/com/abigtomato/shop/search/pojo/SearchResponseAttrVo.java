package com.abigtomato.shop.search.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索接口的返回值（规格属性）
 */
@Data
public class SearchResponseAttrVo implements Serializable {

    // id
    private Long productAttributeId;

    // 属性名称
    private String name;

    // 当前属性对应的所有值
    private List<String> value = new ArrayList<>();
}
