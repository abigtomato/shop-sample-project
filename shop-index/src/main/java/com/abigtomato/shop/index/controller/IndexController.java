package com.abigtomato.shop.index.controller;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.index.service.IndexService;
import com.abigtomato.shop.pms.entity.CategoryEntity;
import com.abigtomato.shop.pms.vo.CategoryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/index")
@Slf4j
@Api(tags = "首页管理")
public class IndexController {

    private IndexService indexService;

    @Autowired
    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @ApiOperation(value = "查询一级分类")
    @GetMapping(value = "/cates")
    public Resp<List<CategoryEntity>> queryLvl1Categories() {
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl1Categories();
        return Resp.ok(categoryEntities);
    }

    @ApiOperation(value = "查询子分类")
    @GetMapping(value = "/cates/{pid}")
    public Resp<List<CategoryVo>> querySubCategories(@PathVariable("pid") Long pid) {
        List<CategoryVo> categoryVOS = this.indexService.querySubCategories(pid);
        return Resp.ok(categoryVOS);
    }

    @ApiOperation(value = "查询子分类V2")
    @GetMapping(value = "/v2/cates/{pid}")
    public Resp<List<CategoryVo>> querySubCategoriesV2(@PathVariable("pid") Long pid) {
        List<CategoryVo> categoryVOS = this.indexService.querySubCategoriesV2(pid);
        return Resp.ok(categoryVOS);
    }
}
