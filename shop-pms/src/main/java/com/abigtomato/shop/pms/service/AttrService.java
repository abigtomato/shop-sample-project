package com.abigtomato.shop.pms.service;

import com.abigtomato.shop.api.pms.entity.AttrEntity;
import com.abigtomato.shop.api.pms.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AttrService extends IService<AttrEntity> {

    void saveAttr(AttrVO attrVO);
}
