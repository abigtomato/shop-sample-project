package com.abigtomato.shop.cart.interceptors;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.core.bean.UserInfo;
import com.abigtomato.shop.core.utils.CookieUtil;
import com.abigtomato.shop.core.utils.CookieUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();

        String userKey = CookieUtils.getCookieValue(request, "user-key");
        if (StrUtil.isEmpty(userKey)) {
            userKey = UUID.randomUUID().toString().replaceAll("-", "");
            CookieUtils.setCookie(request, response, "user-key", userKey, 6 * 30 * 24 * 3600);
            userInfo.setUserKey(userKey);
        }

        String token = CookieUtils.getCookieValue(request, "uid");
        if (StrUtil.isEmpty(token)) {
            // todo 判断token
            userInfo.setToken(token);
        }

        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove();
    }
}
