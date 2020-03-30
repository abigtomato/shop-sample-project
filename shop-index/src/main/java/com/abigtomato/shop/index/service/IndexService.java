package com.abigtomato.shop.index.service;

import com.abigtomato.shop.api.pms.entity.CategoryEntity;
import com.abigtomato.shop.api.pms.vo.CategoryVO;

import java.util.List;

public interface IndexService {

    List<CategoryEntity> queryLvl1Categories();

    List<CategoryVO> querySubCategories(Long pid);

    List<CategoryVO> querySubCategoriesV2(Long pid);
}
