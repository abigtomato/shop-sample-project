package com.abigtomato.shop.sms.service;

import com.abigtomato.shop.api.sms.entity.SkuBoundsEntity;
import com.abigtomato.shop.api.sms.vo.SkuSaleVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    void saveSale(SkuSaleVO skuSaleVO);
}
