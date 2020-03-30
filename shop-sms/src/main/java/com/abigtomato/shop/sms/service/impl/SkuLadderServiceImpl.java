package com.abigtomato.shop.sms.service.impl;

import com.abigtomato.shop.api.sms.entity.SkuLadderEntity;
import com.abigtomato.shop.sms.mapper.SkuLadderMapper;
import com.abigtomato.shop.sms.service.SkuLadderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service(value = "skuLadderService")
@Slf4j
public class SkuLadderServiceImpl extends ServiceImpl<SkuLadderMapper, SkuLadderEntity> implements SkuLadderService {
}
