package com.abigtomato.shop.auth.config;

import com.abigtomato.shop.auth.pojo.UserJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义用户身份验证转换器
 */
@Component
public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    private UserDetailsService userDetailsService;

    @Autowired
    public CustomUserAuthenticationConverter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 验证用户身份（自定义响应）
     * @param authentication
     * @return
     */
    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        LinkedHashMap<String, String> response = new LinkedHashMap<>();
        // 获取用户名
        String name = authentication.getName();

        Object principal = authentication.getPrincipal();
        UserJwt userJwt;
        if (principal instanceof UserJwt) {
            userJwt = (UserJwt) principal;
        } else {
            // refresh_token默认不去调用userDetailsService获取用户信息
            // 这里手动去调用loadUserByUsername验证用户身份的方法，得到UserJwt
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(name);
            userJwt = (UserJwt) userDetails;
        }

        // 自定义响应体
        response.put("user_name", name);
        response.put("name", userJwt.getName());
        response.put("id", userJwt.getId());
        response.put("utype", userJwt.getUtype());
        response.put("userpic", userJwt.getUserpic());
        response.put("companyId", userJwt.getCompanyId());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put("authorities", String.valueOf(AuthorityUtils.authorityListToSet(authentication.getAuthorities())));
        }
        return response;
    }
}
