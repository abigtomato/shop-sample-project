package com.abigtomato.shop.pms.vo;

import com.abigtomato.shop.pms.entity.AttrAttrgroupRelationEntity;
import com.abigtomato.shop.pms.entity.AttrEntity;
import com.abigtomato.shop.pms.entity.AttrGroupEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupVO extends AttrGroupEntity {

    private List<AttrEntity> attrEntities;

    private List<AttrAttrgroupRelationEntity> relationEntities;
}
