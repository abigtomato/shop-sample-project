package com.abigtomato.shop.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.auth.client.UserClient;
import com.abigtomato.shop.auth.pojo.UserJwt;
import com.abigtomato.shop.model.ums.XcMenu;
import com.abigtomato.shop.model.ums.ext.XcUserExt;
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

        XcUserExt userext = this.userClient.getUserext(username);
        if (userext == null) {
            return null;
        }

        String password = userext.getPassword();
        List<XcMenu> permissions = userext.getPermissions();
        if (CollUtil.isEmpty(permissions)) {
            permissions = new ArrayList<>();
        }

        List<String> userPermission = permissions.stream()
                .map(XcMenu::getCode)
                .collect(Collectors.toList());
        String userPermissionStr = StrUtil.join(",", userPermission);

        UserJwt userDetails = new UserJwt(username, password,
                AuthorityUtils.commaSeparatedStringToAuthorityList(userPermissionStr));
        userDetails.setId(userext.getId());
        userDetails.setUtype(userext.getUtype());   // 用户类型
        userDetails.setCompanyId(userext.getCompanyId());   // 所属企业
        userDetails.setName(userext.getName()); // 用户名称
        userDetails.setUserpic(userext.getUserpic());   // 用户头像

        return userDetails;
    }
}
