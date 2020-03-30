package com.abigtomato.shop.api.pms.vo;

import com.abigtomato.shop.api.pms.entity.AttrAttrgroupRelationEntity;
import com.abigtomato.shop.api.pms.entity.AttrEntity;
import com.abigtomato.shop.api.pms.entity.AttrGroupEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupVO extends AttrGroupEntity {

    private List<AttrEntity> attrEntities;

    private List<AttrAttrgroupRelationEntity> relationEntities;
}
