package com.abigtomato.shop.search.listener;

import com.abigtomato.shop.api.pms.entity.*;
import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.search.feign.ShopPmsClient;
import com.abigtomato.shop.search.feign.ShopWmsClient;
import com.abigtomato.shop.search.pojo.GoodsAttr;
import com.abigtomato.shop.search.pojo.GoodsDoc;
import com.abigtomato.shop.search.repository.GoodsRepository;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {

    private GoodsRepository goodsRepository;

    private ShopPmsClient pmsClient;

    private ShopWmsClient wmsClient;

    @Autowired
    public GoodsListener(GoodsRepository goodsRepository,
                         ShopPmsClient pmsClient,
                         ShopWmsClient wmsClient) {
        this.goodsRepository = goodsRepository;
        this.pmsClient = pmsClient;
        this.wmsClient = wmsClient;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "shop-search-queue", durable = "true"),
            exchange = @Exchange(value = "SHOP-PMS-EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.insert", "insert.update"}
    ))
    public void listener(Long spuId) {
        Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkusBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
        if (!CollectionUtils.isEmpty(skuInfoEntities)) {
            // 把sku转化成goods对象
            List<GoodsDoc> goodsList = skuInfoEntities.stream().map(skuInfoEntity -> {
                GoodsDoc goods = new GoodsDoc();

                // 查询搜索属性及值
                Resp<List<ProductAttrValueEntity>> attrValueResp = this.pmsClient.querySearchAttrValueBySpuId(spuId);
                List<ProductAttrValueEntity> attrValueEntities = attrValueResp.getData();
                if (!CollectionUtils.isEmpty(attrValueEntities)) {
                    List<GoodsAttr> searchAttrs = attrValueEntities.stream().map(productAttrValueEntity -> {
                        GoodsAttr searchAttr = new GoodsAttr();
                        searchAttr.setAttrId(productAttrValueEntity.getAttrId());
                        searchAttr.setAttrName(productAttrValueEntity.getAttrName());
                        searchAttr.setAttrValue(productAttrValueEntity.getAttrValue());
                        return searchAttr;
                    }).collect(Collectors.toList());
                    goods.setAttrs(searchAttrs);
                }

                // 查询品牌
                Resp<BrandEntity> brandEntityResp = this.pmsClient.queryBrandById(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                if (brandEntity != null) {
                    goods.setBrandId(skuInfoEntity.getBrandId());
                    goods.setBrandName(brandEntity.getName());
                }

                // 查询分类
                Resp<CategoryEntity> categoryEntityResp = this.pmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(skuInfoEntity.getCatalogId());
                    goods.setCategoryName(categoryEntity.getName());
                }

                Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsClient.querySpuById(spuId);
                SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
                goods.setCreateTime(spuInfoEntity.getCreateTime());
                goods.setPic(skuInfoEntity.getSkuDefaultImg());
                goods.setPrice(skuInfoEntity.getPrice().doubleValue());
                goods.setSale(0L);
                goods.setSkuId(skuInfoEntity.getSkuId());

                // 查询库存信息
                Resp<List<WareSkuEntity>> listResp = this.wmsClient.queryWareSkusBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = listResp.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                    goods.setStore(flag);
                }
                goods.setTitle(skuInfoEntity.getSkuTitle());

                return goods;
            }).collect(Collectors.toList());

            this.goodsRepository.saveAll(goodsList);
        }
    }
}
