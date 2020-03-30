package com.abigtomato.shop.api.pms.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.api.pms.entity.ProductAttrValueEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseAttrVo extends ProductAttrValueEntity {

    public void setValueSelected(List<String> selected) {
        if (CollUtil.isEmpty(selected)) {
            return ;
        }
        this.setAttrValue(StrUtil.join( ",", selected));
    }
}
