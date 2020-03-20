package com.abigtomato.shop.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 服务名称
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ServiceNameEnum {

    pms_service("pms-service"),
    wms_service("wms-service"),
    sms_service("sms-service"),
    ums_service("ums-service"),
    oms_service("oms-service"),
    index_service("index-service"),
    item_service("item-service"),
    search_service("search-service"),
    auth_service("auth-service"),
    ;
    private String name;
}
