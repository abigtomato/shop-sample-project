package com.abigtomato.shop.pms.controller;

import com.abigtomato.shop.core.bean.PageVo;
import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.pms.service.SpuInfoService;
import com.abigtomato.shop.api.pms.vo.SpuInfoVo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "pms/spuinfo")
@Slf4j
@Api(tags = "spu信息管理")
public class SpuInfoController {

    private SpuInfoService spuInfoService;

    @Autowired
    public SpuInfoController(SpuInfoService spuInfoService) {
        this.spuInfoService = spuInfoService;
    }

    @GetMapping(value = "/page")
    public Resp<PageVo> querySpuByPageAndCid(@RequestBody QueryCondition condition,
                                             @RequestParam("catId") Long catId) {
        PageVo pageVo = this.spuInfoService.querySpuByPageAndCid(condition, catId);
        return Resp.ok(pageVo);
    }

    @PostMapping(value = "/save")
    public Resp<Object> spuSave(@RequestBody SpuInfoVo spuInfoVo) {
        spuInfoService.spuSave(spuInfoVo);
        return Resp.ok(null);
    }
}
