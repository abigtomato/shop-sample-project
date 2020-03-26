package com.abigtomato.shop.order.controller;

import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.abigtomato.shop.api.order.OrderApi;
import com.abigtomato.shop.api.order.vo.OrderConfirmVO;
import com.abigtomato.shop.api.order.vo.PayAsyncVo;
import com.abigtomato.shop.api.order.vo.PayVo;
import com.abigtomato.shop.api.wms.vo.SkuLockVO;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.order.pay.AlipayTemplate;
import com.abigtomato.shop.order.service.OrderService;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
@Slf4j
public class OrderController implements OrderApi {

    private OrderService orderService;

    private AmqpTemplate amqpTemplate;

    private StringRedisTemplate redisTemplate;

    private RedissonClient redissonClient;

    private AlipayTemplate alipayTemplate;

    @Autowired
    public OrderController(OrderService orderService,
                           AmqpTemplate amqpTemplate,
                           StringRedisTemplate redisTemplate,
                           RedissonClient redissonClient,
                           AlipayTemplate alipayTemplate) {
        this.orderService = orderService;
        this.amqpTemplate = amqpTemplate;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.alipayTemplate = alipayTemplate;
    }

    @Override
    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm() {
        return Resp.ok(this.orderService.confirm());
    }

    @Override
    @PostMapping("submit")
    public Resp<OrderEntity> submit(@RequestBody OrderSubmitVO submitVO) {
        OrderEntity orderEntity = this.orderService.submit(submitVO);

        try {
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            payVo.setTotal_amount(orderEntity.getPayAmount() != null ? orderEntity.getPayAmount().toString() : "100");
            payVo.setSubject("shop");
            payVo.setBody("pay");
            // 阿里支付
            String form = this.alipayTemplate.pay(payVo);
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return Resp.ok(orderEntity);
    }

    /**
     * 支付成功的回调接口（由支付宝调用）
     * @param payAsyncVo
     * @return
     */
    @Override
    @PostMapping("pay/success")
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo) {
        // 发送支付成功的消息
        this.amqpTemplate.convertAndSend("SHOP-ORDER-EXCHANGE", "order.pay", payAsyncVo.getOut_trade_no());
        return Resp.ok(null);
    }

    @Override
    @PostMapping("seckill/{skuId}")
    public Resp<Object> seckill(Long skuId) {
        // 针对每个商品添加信号量（分布式锁 + 限流）
        RSemaphore semaphore = this.redissonClient.getSemaphore("seckill:semaphore:" + skuId);
        // 设置500并发信号，每来一个线程，都会使信号量减1
        semaphore.trySetPermits(500);

        // 只有成功获取信号的才能继续执行
        if (semaphore.tryAcquire()) {
            // 获取redis中的库存信息
            String countString = this.redisTemplate.opsForValue().get("order:seckill:" + skuId);

            // 没有信息或库存为0则秒杀结束
            if (StringUtils.isEmpty(countString) || Integer.parseInt(countString) == 0) {
                return Resp.ok("秒杀结束");
            }
            int count = Integer.parseInt(countString);

            // 有则减秒杀库存
            this.redisTemplate.opsForValue().set("order:seckill:" + skuId, String.valueOf(--count));

            // 发送消息给消息队列（下单，减库存等流程）
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setCount(1);
            skuLockVO.setSkuId(skuId);
            String orderToken = IdWorker.getIdStr();
            skuLockVO.setOrderToken(orderToken);
            this.amqpTemplate.convertAndSend("SHOP-ORDER-EXCHANGE", "order.seckill", skuLockVO);

            // 添加门闩
            RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:countdown:" + orderToken);
            // 设置一把锁，该锁会在订单流程完成后释放（分布式的倒计时器）
            countDownLatch.trySetCount(1);

            // 释放信号
            semaphore.release();

            // 响应成功
            return Resp.ok("秒杀成功");
        }

        return Resp.ok("秒杀失败");
    }

    @Override
    @GetMapping("seckill/{orderToken}")
    public Resp<Object> querySeckill(String orderToken) throws InterruptedException {
        // 阻塞等待门闩上的锁解开，等待订单完成
        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("seckill:countdown:" + orderToken);
        countDownLatch.await();

        // 查询订单，并响应

        return Resp.ok(null);
    }
}
