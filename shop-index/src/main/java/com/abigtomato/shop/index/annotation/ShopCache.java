package com.abigtomato.shop.index.annotation;

import java.lang.annotation.*;

/**
 * 自定义缓存注解
 */
@Target(ElementType.METHOD) // 作用于方法
@Retention(RetentionPolicy.RUNTIME) // 运行时
@Documented
public @interface ShopCache {

    /**
     * redis缓存key的前缀
     * @return
     */
    String prefix() default "";

    /**
     * 缓存的过期时间，分为单位
     * @return
     */
    int timeout() default 5;

    /**
     * 防止缓存雪崩指定的随机值范围
     * @return
     */
    int random() default 5;
}
