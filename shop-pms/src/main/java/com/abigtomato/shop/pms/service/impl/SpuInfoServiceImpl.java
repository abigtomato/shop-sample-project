package com.abigtomato.shop.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.api.pms.entity.ProductAttrValueEntity;
import com.abigtomato.shop.api.pms.entity.SkuImagesEntity;
import com.abigtomato.shop.api.pms.entity.SkuSaleAttrValueEntity;
import com.abigtomato.shop.api.pms.entity.SpuInfoEntity;
import com.abigtomato.shop.api.sms.vo.SkuSaleVO;
import com.abigtomato.shop.core.bean.PageVo;
import com.abigtomato.shop.core.bean.Query;
import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.pms.feign.ShopSmsClient;
import com.abigtomato.shop.pms.mapper.SpuInfoMapper;
import com.abigtomato.shop.pms.service.*;
import com.abigtomato.shop.api.pms.vo.BaseAttrVo;
import com.abigtomato.shop.api.pms.vo.SkuInfoVo;
import com.abigtomato.shop.api.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service(value = "spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfoEntity> implements SpuInfoService {

    private SpuInfoDescService spuInfoDescService;

    private ProductAttrValueService productAttrValueService;

    private SkuInfoService skuInfoService;

    private SkuImagesService skuImagesService;

    private SkuSaleAttrValueService skuSaleAttrValueService;

    private ShopSmsClient shopSmsClient;

    private AmqpTemplate amqpTemplate;

    @Value(value = "${item.rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Autowired
    public SpuInfoServiceImpl(SpuInfoDescService spuInfoDescService,
                              ProductAttrValueService productAttrValueService,
                              SkuInfoService skuInfoService,
                              SkuImagesService skuImagesService,
                              SkuSaleAttrValueService skuSaleAttrValueService,
                              ShopSmsClient shopSmsClient,
                              AmqpTemplate amqpTemplate) {
        this.spuInfoDescService = spuInfoDescService;
        this.productAttrValueService = productAttrValueService;
        this.skuInfoService = skuInfoService;
        this.skuImagesService = skuImagesService;
        this.skuSaleAttrValueService = skuSaleAttrValueService;
        this.shopSmsClient = shopSmsClient;
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public PageVo querySpuByPageAndCid(QueryCondition condition, Long catId) {
        QueryWrapper<SpuInfoEntity> spuInfoQuery = new QueryWrapper<>();

        if (catId != null && catId != 0) {
            spuInfoQuery.lambda().eq(SpuInfoEntity::getCatalogId, catId);
        }
        
        String key = condition.getKey();
        if (StrUtil.isNotEmpty(key)) {
            spuInfoQuery.lambda()
                    .and(t -> t.eq(SpuInfoEntity::getId, key)
                    .or()
                    .like(SpuInfoEntity::getSpuName, key));
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(condition), spuInfoQuery);
        return new PageVo(page);
    }

    @Override
    @GlobalTransactional    // 开启全局事务
    public void spuSave(SpuInfoVo spuInfoVo) {
        // 1.1 保存pms_spu_info
        Long spuId = this.saveSpuInfo(spuInfoVo);

        // 1.2 保存pms_spu_info_desc
        this.spuInfoDescService.saveSpuInfoDesc(spuInfoVo, spuId);

        // 1.3 保存pms_product_attr_value
        this.saveBaseAttrValue(spuInfoVo, spuId);

        // 2.保存sku相关的3张表
        this.saveSkuAndSale(spuInfoVo, spuId);

        // 3.发送新增类型的消息（更新索引，更新缓存）
        this.sendMsg("insert", spuId);
    }

    private void sendMsg(String type, Long spuId) {
        this.amqpTemplate.convertAndSend(EXCHANGE_NAME, "item" + type, spuId);
    }

    private void saveSkuAndSale(SpuInfoVo spuInfoVo, Long spuId) {
        List<SkuInfoVo> skuInfos = spuInfoVo.getSkuInfos();
        if (CollUtil.isEmpty(skuInfos)) {
            return;
        }

        skuInfos.forEach(skuInfoVo -> {
            // 2.1 保存pms_sku_info
            skuInfoVo.setSpuId(spuId);
            skuInfoVo.setSkuCode(UUID.randomUUID().toString());
            skuInfoVo.setBrandId(skuInfoVo.getBrandId());
            skuInfoVo.setCatalogId(skuInfoVo.getCatalogId());

            List<String> images = skuInfoVo.getImages();
            if (CollUtil.isNotEmpty(images)) {
                // 设置默认图片
                skuInfoVo.setSkuDefaultImg(StrUtil.isNotEmpty(skuInfoVo.getSkuDefaultImg()) ? skuInfoVo.getSkuDefaultImg() : images.get(0));
            }
            this.skuInfoService.getBaseMapper().insert(skuInfoVo);
            Long skuId = skuInfoVo.getSkuId();

            // 2.2 保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)) {
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setSkuId(skuId);
                    // 设置是否默认图片
                    skuImagesEntity.setDefaultImg(StringUtils.equals(skuInfoVo.getSkuDefaultImg(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);
            }

            // 2.3 保存pms_sale_attr_value
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                // 设置skuId
                saleAttrs.forEach(skuSaleAttrValueEntity -> skuSaleAttrValueEntity.setSkuId(skuId));
                // 批量保存销售属性
                this.skuSaleAttrValueService.saveBatch(saleAttrs);
            }

            // 3.保存营销信息的3张表(feign远程调用sms保存)
            SkuSaleVO skuSaleVo = new SkuSaleVO();
            BeanUtils.copyProperties(skuInfoVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.shopSmsClient.saveSale(skuSaleVo);
        });
    }

    private void saveBaseAttrValue(SpuInfoVo spuInfoVo, Long spuId) {
        List<BaseAttrVo> baseAttrs = spuInfoVo.getBaseAttrs();
        if (CollUtil.isEmpty(baseAttrs)) {
            return;
        }

        List<ProductAttrValueEntity> attrValueEntities = baseAttrs.stream().map(baseAttrVo -> {
            baseAttrVo.setSpuId(spuId);
            return (ProductAttrValueEntity) baseAttrVo;
        }).collect(Collectors.toList());

        this.productAttrValueService.saveBatch(attrValueEntities);
    }

    private Long saveSpuInfo(SpuInfoVo spuInfoVo) {
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setCreateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }
}
