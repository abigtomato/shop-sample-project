package com.abigtomato.shop.cart.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.api.cart.model.Cart;
import com.abigtomato.shop.api.pms.entity.SkuInfoEntity;
import com.abigtomato.shop.api.pms.entity.SkuSaleAttrValueEntity;
import com.abigtomato.shop.api.sms.vo.SaleVO;
import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.abigtomato.shop.cart.client.ShopPmsClient;
import com.abigtomato.shop.cart.client.ShopSmsClient;
import com.abigtomato.shop.cart.client.ShopWmsClient;
import com.abigtomato.shop.cart.interceptors.LoginInterceptor;
import com.abigtomato.shop.cart.service.CartService;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.core.bean.UserInfo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    private static final String KEY_PREFIX = "shop:cart:";
    private static final String PRICE_PREFIX = "shop:sku:";

    private ShopPmsClient pmsClient;

    private ShopWmsClient wmsClient;

    private ShopSmsClient smsClient;

    private StringRedisTemplate redisTemplate;

    @Autowired
    public CartServiceImpl(ShopPmsClient pmsClient,
                           ShopWmsClient wmsClient,
                           ShopSmsClient smsClient,
                           StringRedisTemplate redisTemplate) {
        this.pmsClient = pmsClient;
        this.wmsClient = wmsClient;
        this.smsClient = smsClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addCart(Cart cart) {
        String key = this.getLoginState();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        String skuId = cart.getSkuId().toString();
        Integer count = cart.getCount();
        if (this.hasKey(hashOps, skuId)) {
            // 若购物车中已经存在要添加的sku，则更新对应sku的数量
            Object value = hashOps.get(skuId);
            if (value != null) {
                // 反序列化
                cart = JSON.parseObject(value.toString(), Cart.class);
                // 更新购物车中对应商品的数量
                cart.setCount(cart.getCount() + count);
            }
        } else {
            // 若购物车中没有相关sku的记录，则新增
            cart.setCheck(true);    // 更新改变标记，表示购物车被更改

            // 查询sku相关信息
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {
                return ;
            }
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setTitle(skuInfoEntity.getSkuTitle());

            // 查询营销属性
            Resp<List<SkuSaleAttrValueEntity>> listResp = this.pmsClient.querySkuSaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> saleAttrValueEntities = listResp.getData();
            cart.setSaleAttrValues(saleAttrValueEntities);

            // 查询营销信息
            Resp<List<SaleVO>> saleResp = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<SaleVO> saleVOS = saleResp.getData();
            cart.setSales(saleVOS);

            // 查询库存信息
            Resp<List<WareSkuEntity>> wareResp = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResp.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }

            // 存储sku价格到redis中（购物车中商品价格同步）
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuInfoEntity.getPrice().toString());
        }
        // 存储购物车信息到redis中
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    /**
     * 判断key是否存在
     * @param hashOps
     * @param key
     * @return
     */
    private boolean hasKey(BoundHashOperations<String, Object, Object> hashOps, Object key) {
        Boolean flag = hashOps.hasKey(key);
        if (flag != null) {
            return flag;
        }
        return false;
    }

    @Override
    public List<Cart> queryCarts() {
        // 获取登录状态
        UserInfo userInfo = LoginInterceptor.threadLocal.get();

        // 查询未登录状态的购物车
        String unLoginKey = KEY_PREFIX + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(unLoginKey);
        List<Object> cartJsonList = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(cartJsonList)) {
            // 价格同步
            unLoginCarts = cartJsonList.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                // 从redis查询最新价格
                String priceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                if (priceString != null) {
                    cart.setCurrentPrice(new BigDecimal(priceString));
                }
                return cart;
            }).collect(Collectors.toList());
        }

        // 判断是否登录，未登录则直接返回未登录状态的购物车
        if (StrUtil.isEmpty(userInfo.getToken())) {
            return unLoginCarts;
        }

        // 若登录则合并购物车
        String loginKey = KEY_PREFIX + userInfo.getToken();
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                Integer count = cart.getCount();
                Long skuId = cart.getSkuId();
                if (this.hasKey(loginHashOps, skuId)) {
                    Object value = loginHashOps.get(skuId);
                    if (value != null) {
                        cart = JSON.parseObject(value.toString(), Cart.class);
                        cart.setCount(cart.getCount() + count);
                        loginHashOps.put(skuId, JSON.toJSONString(cart));
                    }
                }
            });
            // 删除未登录状态购物车
            this.redisTemplate.delete(unLoginKey);
        }

        // 返回登录状态的购物车
        List<Object> loginCartJsonList = loginHashOps.values();
        if (CollUtil.isEmpty(loginCartJsonList)) {
            return null;
        }
        return loginCartJsonList.stream().map(cartJson -> {
            Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
            // 价格同步
            String priceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
            if (priceString != null) {
                cart.setCurrentPrice(new BigDecimal(priceString));
            }
            return cart;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateCart(Cart cart) {
        // 获取登录状态
        String key = this.getLoginState();

        // 获取购物车
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(key);

        Integer count = cart.getCount();
        Long skuId = cart.getSkuId();
        // 判断更新的这条记录，在购物车中有没有
        if (this.hasKey(boundHashOps, skuId)) {
            Object value = boundHashOps.get(skuId);
            if (value != null) {
                cart = JSON.parseObject(value.toString(), Cart.class);
                cart.setCount(count);
                boundHashOps.put(skuId, JSON.toJSONString(cart));
            }
        }
    }

    @Override
    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        // 获取购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> cartJsonList = hashOps.values();
        if (CollUtil.isEmpty(cartJsonList)) {
            return null;
        }
        // 返回发生变更的购物车列表
        return cartJsonList.stream()
                .map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class))
                .filter(Cart::getCheck)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCart(Long skuId) {
        // 获取登录状态
        String key = this.getLoginState();

        // 获取购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 删除其中的某件sku
        if (this.hasKey(hashOps, skuId)) {
            hashOps.delete(skuId);
        }
    }

    /**
     * 获取登录状态（token或者userKey）
     * @return
     */
    private String getLoginState() {
        String key = "";
        UserInfo userInfo = LoginInterceptor.threadLocal.get();
        if (userInfo.getToken() != null) {
            // 若用户登录，则用token做为购物车的key
            key = KEY_PREFIX + userInfo.getToken();
        } else {
            // 若未登录，则生成userKey做为购物车的key
            key = KEY_PREFIX + userInfo.getUserKey();
        }
        return key;
    }
}
