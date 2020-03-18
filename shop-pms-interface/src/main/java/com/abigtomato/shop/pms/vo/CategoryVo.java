package com.abigtomato.shop.pms.vo;

import com.abigtomato.shop.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVo extends CategoryEntity {

    private List<CategoryEntity> subs;
}
