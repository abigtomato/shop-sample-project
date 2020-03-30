package com.abigtomato.shop.api.ums.model.ext;

import com.abigtomato.shop.api.ums.model.XcMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class XcMenuExt extends XcMenu {

    List<CategoryNode> children;
}
