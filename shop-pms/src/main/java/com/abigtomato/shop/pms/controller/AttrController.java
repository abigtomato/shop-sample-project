package com.abigtomato.shop.pms.controller;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.pms.service.AttrService;
import com.abigtomato.shop.api.pms.vo.AttrVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "pms/attr")
@Slf4j
@Api(tags = "商品属性管理")
public class AttrController {

    private AttrService attrService;

    @Autowired
    public AttrController(AttrService attrService) {
        this.attrService = attrService;
    }

    @PostMapping(value = "/save")
    public Resp<Object> save(@RequestBody AttrVO attrVO) {
        this.attrService.saveAttr(attrVO);
        return Resp.ok(null);
    }
}
