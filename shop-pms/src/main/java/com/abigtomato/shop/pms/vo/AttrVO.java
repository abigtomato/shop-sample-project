package com.abigtomato.shop.pms.vo;

import com.abigtomato.shop.pms.entity.AttrEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AttrVO extends AttrEntity {

    private Long attrGroupId;
}
