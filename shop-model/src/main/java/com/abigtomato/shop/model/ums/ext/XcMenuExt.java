package com.abigtomato.shop.model.ums.ext;

import com.abigtomato.shop.model.ums.XcMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class XcMenuExt extends XcMenu {

    List<CategoryNode> children;
}
