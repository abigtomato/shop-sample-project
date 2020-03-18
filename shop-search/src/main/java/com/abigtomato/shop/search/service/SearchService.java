package com.abigtomato.shop.search.service;

import com.abigtomato.shop.search.pojo.SearchParam;
import com.abigtomato.shop.search.pojo.SearchResponseVo;

import java.io.IOException;

public interface SearchService {

    void create();

    void importData();

    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
