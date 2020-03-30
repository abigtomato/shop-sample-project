package com.abigtomato.shop.pms.controller;

import com.abigtomato.shop.core.bean.PageVo;
import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.pms.service.AttrGroupService;
import com.abigtomato.shop.api.pms.vo.GroupVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "pms/attrgroup")
@Slf4j
@Api(tags = "属性分组管理")
public class AttrGroupController {

    private AttrGroupService attrGroupService;

    @Autowired
    public AttrGroupController(AttrGroupService attrGroupService) {
        this.attrGroupService = attrGroupService;
    }

    @GetMapping(value = "/{catId}")
    public Resp<PageVo> queryAttrGroupsByPageAndCatId(@RequestBody QueryCondition condition,
                                                      @PathVariable("catId") Long catId) {
        PageVo pageVo = this.attrGroupService.queryAttrGroupsByPageAndCatId(condition, catId);
        return Resp.ok(pageVo);
    }

    @GetMapping(value = "/withattr/{gid}")
    public Resp<GroupVO> queryAttrGroupWithAttrsByGid(@PathVariable("gid") Long gid) {
        GroupVO groupVO = this.attrGroupService.queryAttrGroupWithAttrsByGid(gid);
        return Resp.ok(groupVO);
    }
}
