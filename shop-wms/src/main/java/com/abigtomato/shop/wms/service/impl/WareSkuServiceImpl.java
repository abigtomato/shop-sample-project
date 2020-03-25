package com.abigtomato.shop.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.abigtomato.shop.api.wms.vo.SkuLockVO;
import com.abigtomato.shop.wms.mapper.WareSkuMapper;
import com.abigtomato.shop.wms.service.WareSkuService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    private StringRedisTemplate redisTemplate;

    private RedissonClient redissonClient;

    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "stock:lock";

    @Autowired
    public WareSkuServiceImpl(StringRedisTemplate redisTemplate,
                              RedissonClient redissonClient,
                              AmqpTemplate amqpTemplate) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    @Transactional
    public String checkAndLockStore(List<SkuLockVO> skuLockVOS) {
        if (CollUtil.isEmpty(skuLockVOS)) {
            return "没有选中的商品";
        }

        // 检验并锁定库存，并过滤出锁定失败的sku
        List<SkuLockVO> unLockSku = skuLockVOS.stream().peek(skuLockVO -> {
            // 分布式锁保证操作的原子性
            RLock lock = this.redissonClient.getLock("stock:" + skuLockVO.getSkuId());
            lock.lock();

            // 检查库存（真实库存 - 锁定库存 > 购买数量）
            List<WareSkuEntity> wareSkuEntities = this.baseMapper
                    .checkStore(skuLockVO.getSkuId(), skuLockVO.getCount());

            if (CollUtil.isEmpty(wareSkuEntities)) {
                // 库存不足，锁定失败
                skuLockVO.setLock(false);
            } else {
                Long id = wareSkuEntities.get(0).getId();
                // 库存加锁（真实库存不变，锁定库存数量 + 购买数量）
                this.baseMapper.lockStore(id, skuLockVO.getCount());
                skuLockVO.setWareSkuId(id);
                skuLockVO.setLock(true);
            }

            lock.unlock();
        }).filter(skuLockVO -> !skuLockVO.getLock()).collect(Collectors.toList());

        // 若存在锁定失败的商品
        if (CollUtil.isNotEmpty(unLockSku)) {
            // 先解锁锁定成功的商品
            skuLockVOS.stream()
                    .filter(SkuLockVO::getLock)
                    .forEach(skuLockVO -> this.baseMapper.unLockStore(skuLockVO.getWareSkuId(), skuLockVO.getCount()));

            // 返回锁定失败的商品信息
            List<Long> skuIds = unLockSku.stream().map(SkuLockVO::getSkuId).collect(Collectors.toList());
            return "下单失败，存在库存不足的商品：" + skuIds.toString();
        }

        // 将锁定了库存的商品存入redis
        String orderToken = skuLockVOS.get(0).getOrderToken();
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken,
                JSON.toJSONString(skuLockVOS.stream().filter(SkuLockVO::getLock).collect(Collectors.toList())));

        // 锁定成功，发送延时消息，用于定时解锁库存（发送消息到绑定了SHOP-ORDER-EXCHANGE交换机和stock.ttl路由的延时队列中）
        this.amqpTemplate.convertAndSend("SHOP-ORDER-EXCHANGE", "stock.ttl", orderToken);

        return null;
    }
}
