package com.abigtomato.shop.search.controller;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.search.pojo.SearchParam;
import com.abigtomato.shop.search.pojo.SearchResponseVo;
import com.abigtomato.shop.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/search")
@Slf4j
@Api(tags = "搜索服务")
public class SearchController {

    private SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @ApiOperation(value = "创建索引库和映射")
    @PostMapping(value = "/create")
    public Resp<Void> create() {
        this.searchService.create();
        return Resp.ok(null);
    }

    @ApiOperation(value = "导入数据到索引库")
    @PostMapping(value = "/import")
    public Resp<Void> importData() {
        this.searchService.importData();
        return Resp.ok(null);
    }

    @ApiOperation(value = "搜索")
    @GetMapping
    public Resp<SearchResponseVo> search(@RequestBody SearchParam searchParam) throws IOException {
        return Resp.ok(this.searchService.search(searchParam));
    }
}
