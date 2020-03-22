package com.abigtomato.shop.auth;

import com.abigtomato.shop.core.client.ServiceNameList;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * 测试httpclient访问oauth2服务
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class TestClient {

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    private static final String clientId = "XcWebApp";

    private static final String clientSecret = "XcWebApp";

    /**
     * 远程请求认证服务获取令牌
     */
    @Test
    public void testClient() {
        // 从nacos中获取认证服务的地址（因为spring security在认证服务中）
        ServiceInstance serviceInstance = loadBalancerClient.choose(ServiceNameList.AUTH_SERVICE);
        // 此地址就是 http://ip:port
        URI uri = serviceInstance.getUri();
        // 令牌申请的地址 http://ip:port/auth/oauth/token
        String authUrl = uri+ "/auth/oauth/token";

        // 定义header
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = this.getHttpBasic();
        header.add("Authorization", httpBasic); // http basic认证

        // 定义body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", "abigtomato");
        body.add("password", "123456");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);

        // 设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = this.restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        // 申请令牌信息
        Map bodyMap = exchange.getBody();
        System.out.println(bodyMap);
    }

    /**
     * 获取http basic的串
     * @return
     */
    private String getHttpBasic() {
        String string = clientId + ":" + clientSecret;
        // 将串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }

    /**
     * 密码加密
     */
    @Test
    public void testPasswordEncoder() {
        // 原始密码
        String password = "123456";

        // 使用BCrypt加密，每次加密使用一个随机盐
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        for (int i = 0; i < 10; i++) {
            String encode = bCryptPasswordEncoder.encode(password);
            log.info(encode);

            // 校验
            boolean matches = bCryptPasswordEncoder.matches(password, encode);
            System.out.println(matches);
        }
    }
}
