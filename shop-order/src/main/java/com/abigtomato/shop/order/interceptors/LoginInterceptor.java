package com.abigtomato.shop.order.interceptors;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.core.bean.UserInfo;
import com.abigtomato.shop.core.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final String REDIS_KEY_PREFIX = "user_token:";

    public static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    private StringRedisTemplate redisTemplate;

    @Autowired
    public LoginInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 统一获取登陆状态
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();

        // 判断cookie中的userKey
        String userKey = CookieUtils.getCookieValue(request, "user_key");
        if (StrUtil.isEmpty(userKey)) {
            // 不存在则新建一个userKey
            userKey = UUID.randomUUID().toString().replaceAll("-", "");
            CookieUtils.setCookie(request, response, "user_key", userKey, 6 * 30 * 24 * 3600);
            userInfo.setUserKey(userKey);
        }

        // 判断cookie中的身份令牌
        String token = CookieUtils.getCookieValue(request, "uid");
        if (StrUtil.isNotEmpty(token)) {
            // 判断header中的jwt令牌
            String authorization = request.getHeader("Authorization");
            if (StrUtil.isNotEmpty(authorization) &&
                    StrUtil.startWith(authorization, "Bearer ")) {
                // 判断redis中的令牌过期时间
                String key = REDIS_KEY_PREFIX + token;
                Long expire = this.redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (expire != null && expire >= 0) {
                    userInfo.setToken(token);
                }
            }
        }

        threadLocal.set(userInfo);
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 必须手动清除threadLocal中线程变量，因为使用的是tomcat线程池
        threadLocal.remove();
    }
}
