package com.abigtomato.shop.wms.listener;

import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.api.wms.vo.SkuLockVO;
import com.abigtomato.shop.wms.mapper.WareSkuMapper;
import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class WareListener {

    private StringRedisTemplate redisTemplate;

    private WareSkuMapper wareSkuMapper;

    private static final String KEY_PREFIX = "stock:lock";

    @Autowired
    public WareListener(StringRedisTemplate redisTemplate,
                        WareSkuMapper wareSkuMapper) {
        this.redisTemplate = redisTemplate;
        this.wareSkuMapper = wareSkuMapper;
    }

    /**
     * 监听SHOP-ORDER-EXCHANGE交换机，通过路由主键stock.unlock消费：
     *  - 来自正常队列的消息：下单异常解锁库存（保证最终一致性）
     *  - 来自死信队列的消息：超时解锁库存，超时关单解锁库存
     * @param orderToken
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "WMS-UNLOCK-QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP-ORDER-EXCHANGE",
                    ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.unlock"}
    ))
    public void unlockListener(String orderToken) {
        // 根据orderToken从redis中获取被锁库存的商品
        String lockJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if (StrUtil.isEmpty(lockJson)) {
            return ;
        }

        // 库存解锁
        List<SkuLockVO> skuLockVOS = JSON.parseArray(lockJson, SkuLockVO.class);
        skuLockVOS.forEach(skuLockVO -> this.wareSkuMapper.unLockStore(skuLockVO.getWareSkuId(), skuLockVO.getCount()));

        // 删除redis中的记录
        this.redisTemplate.delete(KEY_PREFIX + orderToken);
    }

    /**
     * 监听消息，减库存
     * @param orderToken
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "WMS-MINUS-QUEUE", durable = "true"),
            exchange = @Exchange(value = "SHOP-ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void minusStoreListener(String orderToken) {
        // 根据orderToken从redis中获取被锁库存的商品
        String lockJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if (StrUtil.isEmpty(lockJson)) {
            return ;
        }

        // 减库存
        List<SkuLockVO> skuLockVOS = JSON.parseArray(lockJson, SkuLockVO.class);
        skuLockVOS.forEach(skuLockVO -> this.wareSkuMapper.minusStore(skuLockVO.getWareSkuId(), skuLockVO.getCount()));

        // 删除redis中的记录
        this.redisTemplate.delete(KEY_PREFIX + orderToken);
    }
}
