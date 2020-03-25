package com.abigtomato.shop.wms.controller;

import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.api.wms.WmsApi;
import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.abigtomato.shop.api.wms.vo.SkuLockVO;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.wms.service.WareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("wms/waresku")
@Slf4j
public class WareSkuController implements WmsApi {

    private WareSkuService wareSkuService;

    @Autowired
    public WareSkuController(WareSkuService wareSkuService) {
        this.wareSkuService = wareSkuService;
    }

    @Override
    @GetMapping("{skuId}")
    public Resp<List<WareSkuEntity>> queryWareSkusBySkuId(@PathVariable("skuId") Long skuId) {
        LambdaQueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<WareSkuEntity>()
                .lambda().eq(WareSkuEntity::getSkuId, skuId);
        return Resp.ok(this.wareSkuService.list(queryWrapper));
    }

    @Override
    @PostMapping
    public Resp<Object> checkAndLockStore(@RequestBody List<SkuLockVO> skuLockVOS) {
        String msg = this.wareSkuService.checkAndLockStore(skuLockVOS);
        if (StrUtil.isEmpty(msg)) {
            return Resp.ok(null);
        }
        return Resp.fail(msg);
    }
}
