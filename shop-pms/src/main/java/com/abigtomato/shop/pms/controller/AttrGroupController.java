package com.abigtomato.shop.pms.controller;

import com.abigtomato.shop.core.bean.PageVo;
import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.pms.service.AttrGroupService;
import com.abigtomato.shop.pms.vo.GroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation(value = "")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "", type = ""),
            @ApiImplicitParam(name = "", type = "")
    })
    @GetMapping(value = "/{catId}")
    public Resp<PageVo> queryAttrGroupsByPageAndCatId(@RequestBody QueryCondition condition,
                                                      @PathVariable("catId") Long catId) {
        PageVo pageVo = this.attrGroupService.queryAttrGroupsByPageAndCatId(condition, catId);
        return Resp.ok(pageVo);
    }

    @ApiOperation(value = "")
    @ApiImplicitParam(name = "", type = "")
    @GetMapping(value = "/withattr/{gid}")
    public Resp<GroupVO> queryAttrGroupWithAttrsByGid(@PathVariable("gid") Long gid) {
        GroupVO groupVO = this.attrGroupService.queryAttrGroupWithAttrsByGid(gid);
        return Resp.ok(groupVO);
    }
}
