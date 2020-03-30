package com.abigtomato.shop.pms.service;

import com.abigtomato.shop.api.pms.entity.AttrGroupEntity;
import com.abigtomato.shop.core.bean.PageVo;
import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.api.pms.vo.GroupVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryAttrGroupsByPageAndCatId(QueryCondition condition, Long catId);

    GroupVO queryAttrGroupWithAttrsByGid(Long gid);
}
