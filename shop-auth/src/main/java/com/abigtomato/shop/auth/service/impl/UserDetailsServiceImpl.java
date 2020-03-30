package com.abigtomato.shop.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.api.ums.model.XcMenu;
import com.abigtomato.shop.api.ums.model.ext.XcUserExt;
import com.abigtomato.shop.auth.client.UserClient;
import com.abigtomato.shop.auth.pojo.UserJwt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现spring security提供的UserDetailsService
 */
@Service(value = "userDetailsService")
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserClient userClient;

    private ClientDetailsService clientDetailsService;

    @Autowired
    public UserDetailsServiceImpl(UserClient userClient,
                                  ClientDetailsService clientDetailsService) {
        this.userClient = userClient;
        this.clientDetailsService = clientDetailsService;
    }

    /**
     * spring security在验证用户身份的时候会调用
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            ClientDetails clientDetails = this.clientDetailsService.loadClientByClientId(username);
            if (clientDetails != null) {
                String clientSecret = clientDetails.getClientSecret();
                return new User(username, clientSecret,
                        AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }

        if (StrUtil.isEmpty(username)) {
            return null;
        }

        // 根据用户名获取用户信息
        XcUserExt userext = this.userClient.getUserext(username);
        if (userext == null) {
            return null;
        }

        // 获取密码和用户权限信息
        String password = userext.getPassword();
        List<XcMenu> permissions = userext.getPermissions();
        if (CollUtil.isEmpty(permissions)) {
            permissions = new ArrayList<>();
        }

        List<String> userPermission = permissions.stream()
                .map(XcMenu::getCode)
                .collect(Collectors.toList());
        String userPermissionStr = StrUtil.join(",", userPermission);

        // 封装jwt用户对象
        UserJwt userDetails = new UserJwt(username, password,
                AuthorityUtils.commaSeparatedStringToAuthorityList(userPermissionStr)); // 用户名，密码，权限列表（spring security提供的字段）
        userDetails.setId(userext.getId());
        userDetails.setUtype(userext.getUtype());           // 用户类型
        userDetails.setCompanyId(userext.getCompanyId());   // 所属企业
        userDetails.setName(userext.getName());             // 用户名称
        userDetails.setUserpic(userext.getUserpic());       // 用户头像

        return userDetails;
    }
}
