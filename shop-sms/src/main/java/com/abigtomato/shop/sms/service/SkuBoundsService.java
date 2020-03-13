package com.abigtomato.shop.sms.service;

import com.abigtomato.shop.sms.entity.SkuBoundsEntity;
import com.abigtomato.shop.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    void saveSale(SkuSaleVo skuSaleVO);
}
