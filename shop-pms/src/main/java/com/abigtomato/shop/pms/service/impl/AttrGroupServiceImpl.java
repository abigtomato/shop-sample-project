package com.abigtomato.shop.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.abigtomato.shop.api.pms.entity.AttrAttrgroupRelationEntity;
import com.abigtomato.shop.api.pms.entity.AttrEntity;
import com.abigtomato.shop.api.pms.entity.AttrGroupEntity;
import com.abigtomato.shop.core.bean.PageVo;
import com.abigtomato.shop.core.bean.Query;
import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.pms.mapper.AttrGroupMapper;
import com.abigtomato.shop.pms.service.AttrAttrgroupRelationService;
import com.abigtomato.shop.pms.service.AttrGroupService;
import com.abigtomato.shop.pms.service.AttrService;
import com.abigtomato.shop.api.pms.vo.GroupVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service(value = "attrGroupService")
@Slf4j
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    private AttrService attrService;

    @Autowired
    public AttrGroupServiceImpl(AttrAttrgroupRelationService attrAttrgroupRelationService,
                                AttrService attrService) {
        this.attrAttrgroupRelationService = attrAttrgroupRelationService;
        this.attrService = attrService;
    }

    @Override
    public PageVo queryAttrGroupsByPageAndCatId(QueryCondition condition, Long catId) {
        QueryWrapper<AttrGroupEntity> attrGroupQuery = new QueryWrapper<>();
        if (catId != null) {
            attrGroupQuery.lambda().eq(AttrGroupEntity::getCatelogId, catId);
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(condition), attrGroupQuery);

        return new PageVo(page);
    }

    @Override
    public GroupVO queryAttrGroupWithAttrsByGid(Long gid) {
        GroupVO groupVO = new GroupVO();

        AttrGroupEntity groupEntity = this.getById(gid);
        BeanUtil.copyProperties(groupEntity, groupVO);

        List<AttrAttrgroupRelationEntity> relationEntities = this.attrAttrgroupRelationService.queryRelationsByAttrGroupId(gid);
        if (CollUtil.isEmpty(relationEntities)) {
            return groupVO;
        }
        groupVO.setRelationEntities(relationEntities);

        List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> attrEntities = this.attrService.getBaseMapper().selectBatchIds(attrIds);
        groupVO.setAttrEntities(attrEntities);

        return groupVO;
    }
}
