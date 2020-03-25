package com.abigtomato.shop.order.service.impl;

import com.abigtomato.shop.api.cart.model.Cart;
import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.vo.OrderItemVO;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.abigtomato.shop.api.order.vo.OrderConfirmVO;
import com.abigtomato.shop.api.pms.entity.SkuInfoEntity;
import com.abigtomato.shop.api.pms.entity.SkuSaleAttrValueEntity;
import com.abigtomato.shop.api.ums.entity.MemberEntity;
import com.abigtomato.shop.api.ums.entity.MemberReceiveAddressEntity;
import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.abigtomato.shop.api.wms.vo.SkuLockVO;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.core.bean.UserInfo;
import com.abigtomato.shop.core.exception.OrderException;
import com.abigtomato.shop.order.client.*;
import com.abigtomato.shop.order.interceptors.LoginInterceptor;
import com.abigtomato.shop.order.service.OrderService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private ShopPmsClient pmsClient;

    private ShopOmsClient omsClient;

    private ShopCartClient cartClient;

    private ShopUmsClient umsClient;

    private ShopSmsClient smsClient;

    private ShopWmsClient wmsClient;

    private ThreadPoolExecutor threadPoolExecutor;

    private StringRedisTemplate redisTemplate;

    private AmqpTemplate amqpTemplate;

    private static final String TOKEN_PREFIX = "order:token:";

    @Autowired
    public OrderServiceImpl(ShopPmsClient pmsClient,
                            ShopOmsClient omsClient,
                            ShopCartClient cartClient,
                            ShopUmsClient umsClient,
                            ShopSmsClient smsClient,
                            ShopWmsClient wmsClient,
                            ThreadPoolExecutor threadPoolExecutor,
                            StringRedisTemplate redisTemplate,
                            AmqpTemplate amqpTemplate) {
        this.pmsClient = pmsClient;
        this.omsClient = omsClient;
        this.cartClient = cartClient;
        this.umsClient = umsClient;
        this.smsClient = smsClient;
        this.wmsClient = wmsClient;
        this.threadPoolExecutor = threadPoolExecutor;
        this.redisTemplate = redisTemplate;
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public OrderConfirmVO confirm() {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();

        // 通过拦截器获取用户的登录信息
        UserInfo userInfo = LoginInterceptor.threadLocal.get();
        String token = userInfo.getToken();
        if (userInfo.getToken() == null) {
            return null;
        }

        // 获取用户的收货地址列表（单独线程，异步执行）
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> addressResp = this.umsClient.queryAddressesByUserId(token);
            List<MemberReceiveAddressEntity> memberReceiveAddressEntities = addressResp.getData();
            orderConfirmVO.setAddresses(memberReceiveAddressEntities);
        }, threadPoolExecutor);

        // 获取购物车被勾选的商品信息（单独线程，异步执行）
        CompletableFuture<Void> bigSkuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<Cart>> cartsResp = this.cartClient.queryCheckedCartsByUserId(token);
            List<Cart> cartList = cartsResp.getData();
            if (CollectionUtils.isEmpty(cartList)) {
                throw new OrderException("请勾选购物车商品！");
            }
            return cartList;
        }, threadPoolExecutor).thenAcceptAsync(cartList -> {    // 串行执行，获取购物车内sku的相关信息
            List<OrderItemVO> itemVOS = cartList.stream().map(cart -> {
                OrderItemVO orderItemVO = new OrderItemVO();
                Long skuId = cart.getSkuId();

                // 获取sku基本信息（内嵌，单独，异步）
                CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(skuId);
                    SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                    if (skuInfoEntity != null) {
                        orderItemVO.setWeight(skuInfoEntity.getWeight());
                        orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                        orderItemVO.setPrice(skuInfoEntity.getPrice());
                        orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                        orderItemVO.setSkuId(skuId);
                        orderItemVO.setCount(cart.getCount());
                    }
                }, threadPoolExecutor);

                // 获取sku规格属性（内嵌，单独，异步）
                CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.pmsClient.querySkuSaleAttrValuesBySkuId(skuId);
                    List<SkuSaleAttrValueEntity> attrValueEntities = saleAttrValueResp.getData();
                    orderItemVO.setSaleAttrValues(attrValueEntities);
                }, threadPoolExecutor);

                // 获取sku库存信息（内嵌，单独，异步）
                CompletableFuture<Void> wareSkuCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<WareSkuEntity>> wareSkuResp = this.wmsClient.queryWareSkusBySkuId(skuId);
                    List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                    }
                }, threadPoolExecutor);

                // 等待以上3个线程执行完毕，才会继续接下来的循环
                CompletableFuture.allOf(skuCompletableFuture, saleAttrCompletableFuture, wareSkuCompletableFuture).join();
                return orderItemVO;
            }).collect(Collectors.toList());
            orderConfirmVO.setOrderItems(itemVOS);
        }, threadPoolExecutor);

        // 查询用户信息，获取积分（单独，异步）
        CompletableFuture<Void> memberCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntityResp = this.umsClient.queryMemberById(token);
            MemberEntity memberEntity = memberEntityResp.getData();
            orderConfirmVO.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);

        // 生成一个唯一标志，防止重复提交（单独，异步）
        CompletableFuture<Void> tokenCompletableFuture = CompletableFuture.runAsync(() -> {
            // 雪花算法生成orderToken
            String orderToken = IdWorker.getIdStr();
            // 响应到页面一份
            orderConfirmVO.setOrderToken(orderToken);
            // 保存到redis一份
            this.redisTemplate.opsForValue().set(TOKEN_PREFIX + orderToken, orderToken);
        }, threadPoolExecutor);

        // 主线程阻塞等待所有子任务结束
        CompletableFuture.allOf(addressCompletableFuture, bigSkuCompletableFuture,
                memberCompletableFuture, tokenCompletableFuture).join();
        return orderConfirmVO;
    }

    @Override
    public OrderEntity submit(OrderSubmitVO submitVO) {
        // 通过拦截器获取用户的登录信息
        UserInfo userInfo = LoginInterceptor.threadLocal.get();

        // 获取orderToken
        String orderToken = submitVO.getOrderToken();

        /*
         * 1.订单防重复提交，查询redis中有没有orderToken信息，若有则是第一次提交，放行并删除redis中的orderToken
         */
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(TOKEN_PREFIX + orderToken), orderToken);  // lua脚本保证redis查询和删除操作的原子性
        if (flag != null && flag == 0) {
            throw new OrderException("订单不可重复提交！");
        }

        /*
         * 2.校验价格，页面提交的价格和实时计算的价格一致则放行
         */
        List<OrderItemVO> items = submitVO.getItems();      // 送货清单
        BigDecimal totalPrice = submitVO.getTotalPrice();   // 页面提交的总价
        if (CollectionUtils.isEmpty(items)) {
            throw new OrderException("没有购买的商品，请到购物车中勾选商品！");
        }

        // 计算实时总价
        Optional<BigDecimal> currentTotalPriceOptional = items.stream().map(item -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                return skuInfoEntity.getPrice().multiply(new BigDecimal(item.getCount()));
            }
            return new BigDecimal(0);
        }).reduce(BigDecimal::add);

        if (currentTotalPriceOptional.isPresent()) {
            // 判断实时总价和页面提交的总价是否一致
            if (currentTotalPriceOptional.get().compareTo(totalPrice) != 0) {
                throw new OrderException("页面已过期，请刷新页面后重新下单！");
            }
        }

        /*
         * 3.校验库存是否充足并锁定库存，一次性提示所有库存不够的商品信息
         */
        List<SkuLockVO> lockVOS = items.stream().map(orderItemVO -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItemVO.getSkuId());
            skuLockVO.setCount(orderItemVO.getCount());
            skuLockVO.setOrderToken(orderToken);
            return skuLockVO;
        }).collect(Collectors.toList());

        // 验库锁库
        Resp<Object> wareResp = this.wmsClient.checkAndLockStore(lockVOS);
        if (wareResp.getCode() != 0) {
            throw new OrderException(wareResp.getMsg());
        }

        /*
         * 4.下单，通过最终一致性来实现事务
         */
        Resp<OrderEntity> orderEntityResp;
        try {
            submitVO.setUserId(userInfo.getToken());
            // 创建订单及订单详情
            orderEntityResp = this.omsClient.saveOrder(submitVO);
        } catch (Exception e) {
            e.printStackTrace();
            // 若发生异常，则发送消息给wms，解锁对应的库存
            this.amqpTemplate.convertAndSend("SHOP-ORDER-EXCHANGE", "stock.unlock", orderToken);
            throw new OrderException("服务器错误，创建订单失败！");
        }

        /*
         * 5.删除购物车，发送消息异步删除购物车，避免购物车业务发生的异常影响订单业务
         */
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getToken());
        map.put("skuIds", items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList()));
        // 发送消息删除购物车，异步解耦
        this.amqpTemplate.convertAndSend("SHOP-ORDER-EXCHANGE", "cart.delete", map);

        if (orderEntityResp != null) {
            return orderEntityResp.getData();
        } else {
            return null;
        }
    }
}
