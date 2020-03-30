package com.abigtomato.shop.pms.service.impl;

import com.abigtomato.shop.api.pms.entity.SkuImagesEntity;
import com.abigtomato.shop.pms.mapper.SkuImagesMapper;
import com.abigtomato.shop.pms.service.SkuImagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service(value = "skuImagesService")
@Slf4j
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesMapper, SkuImagesEntity> implements SkuImagesService {
}
