package com.abigtomato.shop.item.service.impl;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.item.feign.ShopPmsClient;
import com.abigtomato.shop.item.feign.ShopSmsClient;
import com.abigtomato.shop.item.feign.ShopWmsClient;
import com.abigtomato.shop.item.service.ItemService;
import com.abigtomato.shop.item.vo.ItemVo;
import com.abigtomato.shop.pms.entity.*;
import com.abigtomato.shop.pms.vo.ItemGroupVo;
import com.abigtomato.shop.sms.vo.SaleVo;
import com.abigtomato.shop.wms.entity.WareSkuEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private ShopPmsClient shopPmsClient;

    private ShopSmsClient shopSmsClient;

    private ShopWmsClient shopWmsClient;

    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    public ItemServiceImpl(ShopPmsClient shopPmsClient,
                           ShopSmsClient shopSmsClient,
                           ShopWmsClient shopWmsClient,
                           ThreadPoolExecutor threadPoolExecutor) {
        this.shopPmsClient = shopPmsClient;
        this.shopSmsClient = shopSmsClient;
        this.shopWmsClient = shopWmsClient;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * 异步编排解决大量远程调用速度慢的问题
     * @param skuId
     * @return
     */
    @Override
    public ItemVo queryItemVo(Long skuId) {
        ItemVo itemVo = new ItemVo();
        itemVo.setSkuId(skuId);

        // 查询sku（单独，异步执行）
        CompletableFuture<Object> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuResp = this.shopPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuResp.getData();
            if (skuInfoEntity == null) {
                return itemVo;
            }
            itemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVo.setSubTitle(skuInfoEntity.getSkuSubtitle());
            itemVo.setPrice(skuInfoEntity.getPrice());
            itemVo.setWeight(skuInfoEntity.getWeight());
            itemVo.setSpuId(skuInfoEntity.getSpuId());
            return skuInfoEntity;
        }, threadPoolExecutor);

        // 根据sku中的spuId查询spu（根据skuCompletableFuture串行执行，也就是等待skuCompletableFuture执行结束，以其返回值作为参数再执行）
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<SpuInfoEntity> spuResp = this.shopPmsClient.querySpuById(((SkuInfoEntity) sku).getSpuId());
            SpuInfoEntity spuInfoEntity = spuResp.getData();
            if (spuInfoEntity != null) {
                itemVo.setSpuName(spuInfoEntity.getSpuName());
            }
        }, threadPoolExecutor);

        // 根据skuId查询图片列表（单独，异步执行）
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SkuImagesEntity>> skuImagesResp = this.shopPmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = skuImagesResp.getData();
            itemVo.setPics(skuImagesEntities);
        }, threadPoolExecutor);

        // 根据sku中brandId查询品牌（根据skuCompletableFuture串行执行）
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<BrandEntity> brandEntityResp = this.shopPmsClient.queryBrandById(((SkuInfoEntity) sku).getBrandId());
            BrandEntity brandEntity = brandEntityResp.getData();
            itemVo.setBrandEntity(brandEntity);
        }, threadPoolExecutor);

        // 根据sku中categoryId查询分类（根据skuCompletableFuture串行执行）
        CompletableFuture<Void> cateCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<CategoryEntity> categoryEntityResp = this.shopPmsClient.queryCategoryById(((SkuInfoEntity) sku).getCatalogId());
            CategoryEntity categoryEntity = categoryEntityResp.getData();
            itemVo.setCategoryEntity(categoryEntity);
        }, threadPoolExecutor);

        // 根据skuId查询营销信息（单独，异步执行）
        CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SaleVo>> salesResp = this.shopSmsClient.querySalesBySkuId(skuId);
            List<SaleVo> saleVOList = salesResp.getData();
            itemVo.setSales(saleVOList);
        }, threadPoolExecutor);

        // 根据skuId查询库存信息（单独，异步执行）
        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<WareSkuEntity>> wareResp = this.shopWmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResp.getData();
            itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        }, threadPoolExecutor);

        // 根据spuId查询所有skuIds，再去查询所有的销售属性（根据skuCompletableFuture串行执行）
        CompletableFuture<Void> saleAttrCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.shopPmsClient.querySkuSaleAttrValuesBySpuId(((SkuInfoEntity) sku).getSpuId());
            List<SkuSaleAttrValueEntity> SkuSaleAttrValueEntities = saleAttrValueResp.getData();
            itemVo.setSaleAttrs(SkuSaleAttrValueEntities);
        }, threadPoolExecutor);

        // 根据spuId查询商品描述（根据skuCompletableFuture串行执行）
        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.shopPmsClient.querySpuDescBySpuId(((SkuInfoEntity) sku).getSpuId());
            SpuInfoDescEntity descEntity = spuInfoDescEntityResp.getData();
            if (descEntity != null) {
                String decript = descEntity.getDecript();
                String[] split = StringUtils.split(decript, ",");
                itemVo.setImages(Arrays.asList(split));
            }
        }, threadPoolExecutor);

        // 根据spuId和cateId查询组及组下规格参数（根据skuCompletableFuture串行执行）
        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(sku -> {
            Resp<List<ItemGroupVo>> itemGroupResp = this.shopPmsClient.queryItemGroupVOByCidAndSpuId(((SkuInfoEntity) sku).getCatalogId(), ((SkuInfoEntity) sku).getSpuId());
            List<ItemGroupVo> itemGroupVOS = itemGroupResp.getData();
            itemVo.setGroups(itemGroupVOS);
        });

        // allOf表示等待所有任务完成
        // join表示将异步任务的逻辑与main中的逻辑拼接，使main等待异步任务完成才会结束
        CompletableFuture.allOf(spuCompletableFuture, imageCompletableFuture, brandCompletableFuture,
                cateCompletableFuture, saleCompletableFuture, storeCompletableFuture,
                saleAttrCompletableFuture, descCompletableFuture, groupCompletableFuture).join();

        return itemVo;
    }
}
