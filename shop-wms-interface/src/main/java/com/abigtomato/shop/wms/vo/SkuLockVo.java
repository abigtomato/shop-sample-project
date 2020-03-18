package com.abigtomato.shop.wms.vo;

import lombok.Data;

@Data
public class SkuLockVo {

    private Long skuId;
    private Integer count;
    private Long wareSkuId; // 锁定库存的id
    private Boolean lock; // 锁定状态
    private String orderToken;
}
