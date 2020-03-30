package com.abigtomato.shop.cart.listener;

import com.abigtomato.shop.api.pms.entity.SkuInfoEntity;
import com.abigtomato.shop.cart.client.ShopPmsClient;
import com.abigtomato.shop.core.bean.Resp;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CartListener {

    private ShopPmsClient pmsClient;

    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "shop:cart:";

    private static final String PRICE_PREFIX = "shop:sku:";

    @Autowired
    public CartListener(ShopPmsClient pmsClient,
                        StringRedisTemplate redisTemplate) {
        this.pmsClient = pmsClient;
        this.redisTemplate = redisTemplate;
    }

    /**
     * pms服务中商品价格发送变动时发送消息
     * 监听器收到消息后更新redis中保存的sku最新价格，用于购物车的价格同步
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "CART-ITEM-QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP-PMS-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.update"}
    ))
    public void updateListener(Long spuId) {
        // 查询sku
        Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkusBySpuId(spuId);

        // 更新redis中的最新价格
        skuResp.getData().forEach(skuInfoEntity -> this.redisTemplate.opsForValue()
                .set(PRICE_PREFIX + skuInfoEntity.getSkuId(), skuInfoEntity.getPrice().toString()));
    }

    /**
     * 监听消息删除购物车
     * @param map
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-CART-QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP-ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void deleteListener(Map<String, Object> map) {
        // 获取参数信息
        Long userId = (Long) map.get("userId");
        List<Object> skuIds = (List<Object>) map.get("skuIds");

        // 获取价格信息
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        // 删除sku价格
        hashOps.delete(skuIds);
    }
}
