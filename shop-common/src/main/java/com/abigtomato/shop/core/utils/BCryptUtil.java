package com.abigtomato.shop.core.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt工具类
 */
public class BCryptUtil {

    /**
     * 加密
     * @param password
     * @return
     */
    public static String encode(String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 匹配
     * @param password
     * @param hashPass
     * @return
     */
    public static boolean matches(String password,String hashPass) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(password, hashPass);
    }
}
