package com.abigtomato.shop.pms.service.impl;

import com.abigtomato.shop.pms.entity.SkuSaleAttrValueEntity;
import com.abigtomato.shop.pms.mapper.SkuSaleAttrValueMapper;
import com.abigtomato.shop.pms.service.SkuSaleAttrValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service(value = "skuSaleAttrValueService")
@Slf4j
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueMapper, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {
}
