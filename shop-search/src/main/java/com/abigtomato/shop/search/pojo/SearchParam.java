package com.abigtomato.shop.search.pojo;

import lombok.Data;

/**
 * 搜索接口的参数
 * search?keyword=手机&catelog3=1&brand=2&props=43:3g-4g-5g&props=45:4.7-5.0&order=2:asc/desc&priceFrom=100&priceTo=10000&pageNum=1&pageSize=12
 */
@Data
public class SearchParam {

    // 检索的关键字
    private String keyword;

    // 三级分类id
    private String[] catelog3;

    // 品牌id
    private String[] brand;

    // 排序参数：order=1:asc/0:asc，0:综合排序 1:销量 2:价格
    private String order;

    // 分页参数
    private Integer pageNum = 1;
    private Integer pageSize = 12;

    // 规格参数：props=2:青年-老人-女士&props=2:ios-android&props=3:4g&props=4:5.5
    private String[] props;

    // 价格区间
    private Integer priceFrom;
    private Integer priceTo;
}
