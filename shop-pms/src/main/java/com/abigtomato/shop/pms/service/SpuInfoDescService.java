package com.abigtomato.shop.pms.service;

import com.abigtomato.shop.api.pms.entity.SpuInfoDescEntity;
import com.abigtomato.shop.api.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    void saveSpuInfoDesc(SpuInfoVo spuInfoVo, Long spuId);
}
