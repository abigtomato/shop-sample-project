package com.abigtomato.shop.search.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索接口的返回值
 */
@Data
public class SearchResponseVo implements Serializable {

    // 品牌json
    private SearchResponseAttrVo brand;

    // 分类json
    private SearchResponseAttrVo catelog;

    // 规格属性json
    private List<SearchResponseAttrVo> attrs = new ArrayList<>();

    // 商品信息json
    private List<GoodsDoc> products = new ArrayList<>();

    // 总记录数
    private Long total;

    // 每页显示的条目数
    private Integer pageSize;

    // 当前页码
    private Integer pageNum;
}
