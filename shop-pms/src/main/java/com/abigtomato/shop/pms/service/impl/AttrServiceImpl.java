package com.abigtomato.shop.pms.service.impl;

import com.abigtomato.shop.api.pms.entity.AttrAttrgroupRelationEntity;
import com.abigtomato.shop.api.pms.entity.AttrEntity;
import com.abigtomato.shop.pms.mapper.AttrMapper;
import com.abigtomato.shop.pms.service.AttrAttrgroupRelationService;
import com.abigtomato.shop.pms.service.AttrService;
import com.abigtomato.shop.api.pms.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "attrServiceImpl")
@Slf4j
public class AttrServiceImpl extends ServiceImpl<AttrMapper, AttrEntity> implements AttrService {

    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    public AttrServiceImpl(AttrAttrgroupRelationService attrAttrgroupRelationService) {
        this.attrAttrgroupRelationService = attrAttrgroupRelationService;
    }

    @Override
    public void saveAttr(AttrVO attrVO) {
        this.save(attrVO);
        Long attrId = attrVO.getAttrId();

        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrId(attrId);
        relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
        this.attrAttrgroupRelationService.getBaseMapper().insert(relationEntity);
    }
}
