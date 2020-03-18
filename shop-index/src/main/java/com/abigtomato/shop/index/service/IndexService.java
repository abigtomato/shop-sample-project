package com.abigtomato.shop.index.service;

import com.abigtomato.shop.pms.entity.CategoryEntity;
import com.abigtomato.shop.pms.vo.CategoryVo;

import java.util.List;

public interface IndexService {

    List<CategoryEntity> queryLvl1Categories();

    List<CategoryVo> querySubCategories(Long pid);

    List<CategoryVo> querySubCategoriesV2(Long pid);
}
