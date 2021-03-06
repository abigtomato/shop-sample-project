package com.abigtomato.shop.api.ums.model.ext;

import com.abigtomato.shop.api.ums.model.XcMenu;
import com.abigtomato.shop.api.ums.model.XcUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class XcUserExt extends XcUser {

    //权限信息
    private List<XcMenu> permissions;

    //企业信息
    private String companyId;
}
