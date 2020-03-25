package com.abigtomato.shop.wms.service;

import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.abigtomato.shop.api.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface WareSkuService extends IService<WareSkuEntity> {

    /**
     * 检查并锁定库存
     * @param skuLockVOS
     * @return
     */
    String checkAndLockStore(List<SkuLockVO> skuLockVOS);
}
