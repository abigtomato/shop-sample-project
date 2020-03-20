package com.abigtomato.shop.core.response;

import lombok.Data;

import java.util.List;

@Data
public class QueryResult<T> {

    private List<T> list;   // 数据列表

    private long total;     // 数据总数
}
