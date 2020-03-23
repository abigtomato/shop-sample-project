package com.abigtomato.shop.api.pms.vo;

import com.abigtomato.shop.api.pms.entity.CategoryEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryVO extends CategoryEntity {

    private List<CategoryEntity> subs;
}
