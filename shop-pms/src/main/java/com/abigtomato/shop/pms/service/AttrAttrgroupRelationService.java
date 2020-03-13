package com.abigtomato.shop.pms.service;

import com.abigtomato.shop.pms.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    List<AttrAttrgroupRelationEntity> queryRelationsByAttrGroupId(Long attrGroupId);
}
