package com.abigtomato.shop.pms.service.impl;

import com.abigtomato.shop.api.pms.entity.SkuInfoEntity;
import com.abigtomato.shop.pms.mapper.SkuInfoMapper;
import com.abigtomato.shop.pms.service.SkuInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service(value = "skuInfoService")
@Slf4j
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfoEntity> implements SkuInfoService {
}
