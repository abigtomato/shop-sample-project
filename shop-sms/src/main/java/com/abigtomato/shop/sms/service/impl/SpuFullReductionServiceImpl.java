package com.abigtomato.shop.sms.service.impl;

import com.abigtomato.shop.sms.entity.SpuFullReductionEntity;
import com.abigtomato.shop.sms.mapper.SpuFullReductionMapper;
import com.abigtomato.shop.sms.service.SpuFullReductionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service(value = "spuFullReductionService")
@Slf4j
public class SpuFullReductionServiceImpl extends ServiceImpl<SpuFullReductionMapper, SpuFullReductionEntity> implements SpuFullReductionService {
}
