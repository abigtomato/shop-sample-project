package com.abigtomato.shop.pms.service.impl;

import com.abigtomato.shop.api.pms.entity.AttrAttrgroupRelationEntity;
import com.abigtomato.shop.pms.mapper.AttrAttrgroupRelationMapper;
import com.abigtomato.shop.pms.service.AttrAttrgroupRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(value = "attrAttrgroupRelationService")
@Slf4j
public class AttrAttrgroupRelationServiceImpl
        extends ServiceImpl<AttrAttrgroupRelationMapper, AttrAttrgroupRelationEntity>
        implements AttrAttrgroupRelationService {

    @Override
    public List<AttrAttrgroupRelationEntity> queryRelationsByAttrGroupId(Long attrGroupId) {
        return this.list(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .lambda().eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupId));
    }
}
