package com.abigtomato.shop.sms.service.impl;

import com.abigtomato.shop.api.sms.entity.SkuBoundsEntity;
import com.abigtomato.shop.api.sms.entity.SkuFullReductionEntity;
import com.abigtomato.shop.api.sms.entity.SkuLadderEntity;
import com.abigtomato.shop.api.sms.vo.SkuSaleVO;
import com.abigtomato.shop.sms.mapper.SkuBoundsMapper;
import com.abigtomato.shop.sms.service.SkuBoundsService;
import com.abigtomato.shop.sms.service.SkuLadderService;
import com.abigtomato.shop.sms.service.SpuFullReductionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service(value = "skuBoundsService")
@Slf4j
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    private SkuLadderService skuLadderService;

    private SpuFullReductionService spuFullReductionService;

    @Autowired
    public SkuBoundsServiceImpl(SkuLadderService skuLadderService,
                                SpuFullReductionService spuFullReductionService) {
        this.skuLadderService = skuLadderService;
        this.spuFullReductionService = spuFullReductionService;
    }

    @Override
    @Transactional  // 开启本地事务
    public void saveSale(SkuSaleVO skuSaleVo) {
        // 3.1. 保存sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(skuSaleVo.getSkuId());
        skuBoundsEntity.setGrowBounds(skuSaleVo.getGrowBounds());
        skuBoundsEntity.setBuyBounds(skuSaleVo.getBuyBounds());
        List<Integer> work = skuSaleVo.getWork();
        skuBoundsEntity.setWork(work.get(3) + work.get(2) * 2 + work.get(1) * 4 + work.get(0) * 8);
        this.save(skuBoundsEntity);

        // 3.2. 保存sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuSaleVo.getSkuId());
        skuLadderEntity.setFullCount(skuSaleVo.getFullCount());
        skuLadderEntity.setDiscount(skuSaleVo.getDiscount());
        skuLadderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        this.skuLadderService.getBaseMapper().insert(skuLadderEntity);

        // 3.3. 保存sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        reductionEntity.setSkuId(skuSaleVo.getSkuId());
        reductionEntity.setFullPrice(skuSaleVo.getFullPrice());
        reductionEntity.setReducePrice(skuSaleVo.getReducePrice());
        reductionEntity.setAddOther(skuSaleVo.getFullAddOther());
//        this.spuFullReductionService.getBaseMapper().insert(reductionEntity);
    }
}