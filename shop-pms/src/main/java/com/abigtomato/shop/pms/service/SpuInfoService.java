package com.abigtomato.shop.pms.service;

import com.abigtomato.shop.api.pms.entity.SpuInfoEntity;
import com.abigtomato.shop.core.bean.PageVo;
import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.api.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo querySpuByPageAndCid(QueryCondition condition, Long catId);

    void spuSave(SpuInfoVo spuInfoVo);
}
