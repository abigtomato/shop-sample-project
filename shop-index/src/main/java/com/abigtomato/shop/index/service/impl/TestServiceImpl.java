package com.abigtomato.shop.index.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.index.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TestServiceImpl implements TestService {

    @Resource
    private RedissonClient redissonClient;

    private StringRedisTemplate redisTemplate;

    @Autowired
    public TestServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 使用redisson提供的lock对redis操作加分布式锁
     * @return
     */
    @Override
    public String testLock() {
        // 获取锁
        RLock lock = this.redissonClient.getLock("lock");
        // 加锁操作
        lock.lock();

        // 中间的操作由获取锁的线程执行
        String numStr = this.redisTemplate.opsForValue().get("num");
        if (!StrUtil.isEmpty(numStr)) {
            int num = Integer.parseInt(numStr);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
        }

        // 释放锁
        lock.unlock();
        return numStr;
    }

    /**
     * 使用redis的setnx命令对redis的操作加分布式锁
     * @return
     */
    @Override
    public String testLockV2() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        // 执行到此的所有线程都会循环不断得尝试获取锁
        boolean flag = false;
        do {
            // setnx命令只能设置一次，再次设置会操作失败，可以当作lock使用
            Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 5, TimeUnit.SECONDS);
            if (lock != null) {
                flag = lock;
            }
        } while (!flag);

        // 执行需要加锁的操作
        String numStr = this.redisTemplate.opsForValue().get("num");
        if (!StrUtil.isEmpty(numStr)) {
            int num = Integer.parseInt(numStr);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
        }

        // 使用lua脚本删除lock，保证删除当前线程的锁，且维持操作的原子性
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        this.redisTemplate.execute(new DefaultRedisScript<>(script), CollUtil.newArrayList("lock"), uuid);

        return numStr;
    }

    /**
     * 读锁测试
     * @return
     */
    @Override
    public String testRead() {
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");

        // 读锁并发
        rwLock.readLock().lock(10L, TimeUnit.SECONDS);
        String test = this.redisTemplate.opsForValue().get("test");
        rwLock.readLock().unlock();

        return test;
    }

    /**
     * 写锁测试
     * @return
     */
    @Override
    public String testWrite() {
        RReadWriteLock rwLock = this.redissonClient.getReadWriteLock("rwLock");

        // 写锁互斥
        rwLock.writeLock().lock(10L, TimeUnit.SECONDS);
        this.redisTemplate.opsForValue().set("test", UUID.randomUUID().toString());
        rwLock.writeLock().unlock();

        return "write success";
    }

    /**
     * 门闩加锁
     * @return
     * @throws InterruptedException
     */
    @Override
    public String testLatch() throws InterruptedException {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        // 添加5把锁
        latch.trySetCount(5);
        // 等待锁全部释放
        latch.await();
        return "主业务开始执行";
    }

    /**
     * 释放门闩上的一个锁
     * @return
     */
    @Override
    public String testCountdown() {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        // 释放一把锁
        latch.countDown();
        return "分页业务执行一次";
    }
}
