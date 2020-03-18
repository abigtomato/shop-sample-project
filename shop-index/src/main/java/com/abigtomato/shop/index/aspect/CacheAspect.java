package com.abigtomato.shop.index.aspect;

import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.index.annotation.ShopCache;
import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 自定义aop
 */
@Aspect
@Component
public class CacheAspect {

    private StringRedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    public CacheAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 环绕模式
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.abigtomato.shop.index.annotation.ShopCache)")  // 指定作用的注解
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        // 获取目标方法
        Method method = signature.getMethod();

        // 获取方法的注解
        ShopCache shopCache = method.getAnnotation(ShopCache.class);
        String prefix = shopCache.prefix();
        // 获取方法的参数列表
        Object[] args = pjp.getArgs();
        String key = prefix + Arrays.asList(args).toString();

        // 获取方法的返回值
        Class<?> returnType = method.getReturnType();
        // 尝试从缓存中获取
        Optional<Object> optional = this.cacheHit(key, returnType);
        if (optional.isPresent()) {
            return optional.get();
        }

        // 获取锁
        RLock lock = this.redissonClient.getLock("lock" + Arrays.asList(args).toString());
        lock.lock();

        // 再次尝试从缓存中获取数据（当前线程获取锁后进入该处，若是其他线程已经操作完之后的逻辑，需要再次尝试获取缓存）
        optional = this.cacheHit(key, returnType);
        if (optional.isPresent()) {
            lock.unlock();
            return optional.get();
        }

        // 执行目标方法
        Object result = pjp.proceed(args);

        // 获取注解的属性，超时时间和随机数范围
        int timeout = shopCache.timeout();
        int random = shopCache.random();
        // 写入redis缓存，设置的超时时间需要额外加上随机数（防止出现雪崩问题）
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result),
                timeout + new Random().nextInt(random), TimeUnit.MINUTES);

        // 释放锁
        lock.unlock();
        return result;
    }

    private Optional<Object> cacheHit(String key, Class<?> returnType) {
        String value = this.redisTemplate.opsForValue().get(key);
        if (StrUtil.isEmpty(value)) {
            return Optional.empty();
        }
        return Optional.of(JSON.parseObject(value, returnType));
    }
}
