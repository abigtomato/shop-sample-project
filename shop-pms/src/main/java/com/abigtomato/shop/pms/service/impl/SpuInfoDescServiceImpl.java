package com.abigtomato.shop.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.abigtomato.shop.pms.entity.SpuInfoDescEntity;
import com.abigtomato.shop.pms.mapper.SpuInfoDescMapper;
import com.abigtomato.shop.pms.service.SpuInfoDescService;
import com.abigtomato.shop.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service(value = "spuInfoDescService")
@Slf4j
public class SpuInfoDescServiceImpl extends ServiceImpl<SpuInfoDescMapper, SpuInfoDescEntity> implements SpuInfoDescService {

    @Override
    @Transactional  // 开启本地事务
    public void saveSpuInfoDesc(SpuInfoVo spuInfoVo, Long spuId) {
        List<String> spuImages = spuInfoVo.getSpuImages();
        if (CollUtil.isEmpty(spuImages)) {
            return;
        }
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuId);
        descEntity.setDecript(StringUtils.join(spuImages, ","));
        this.save(descEntity);
    }
}
