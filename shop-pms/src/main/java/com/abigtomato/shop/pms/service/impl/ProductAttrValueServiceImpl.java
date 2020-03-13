package com.abigtomato.shop.pms.service.impl;

import com.abigtomato.shop.pms.entity.ProductAttrValueEntity;
import com.abigtomato.shop.pms.mapper.ProductAttrValueMapper;
import com.abigtomato.shop.pms.service.ProductAttrValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service(value = "productAttrValueService")
@Slf4j
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueMapper, ProductAttrValueEntity> implements ProductAttrValueService {
}
