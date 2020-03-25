package com.abigtomato.shop.oms.service.impl;

import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.abigtomato.shop.api.oms.entity.OrderItemEntity;
import com.abigtomato.shop.api.oms.vo.OrderItemVO;
import com.abigtomato.shop.api.oms.vo.OrderSubmitVO;
import com.abigtomato.shop.api.pms.entity.SkuInfoEntity;
import com.abigtomato.shop.api.pms.entity.SpuInfoEntity;
import com.abigtomato.shop.api.ums.entity.MemberEntity;
import com.abigtomato.shop.api.ums.entity.MemberReceiveAddressEntity;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.oms.client.ShopPmsClient;
import com.abigtomato.shop.oms.client.ShopUmsClient;
import com.abigtomato.shop.oms.mapper.OrderItemMapper;
import com.abigtomato.shop.oms.mapper.OrderMapper;
import com.abigtomato.shop.oms.service.OmsService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class OmsServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OmsService {

    private ShopUmsClient umsClient;

    private ShopPmsClient pmsClient;

    private OrderItemMapper itemMapper;

    private AmqpTemplate amqpTemplate;

    @Autowired
    public OmsServiceImpl(ShopUmsClient umsClient,
                          ShopPmsClient pmsClient,
                          OrderItemMapper itemMapper,
                          AmqpTemplate amqpTemplate) {
        this.umsClient = umsClient;
        this.pmsClient = pmsClient;
        this.itemMapper = itemMapper;
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    @Transactional
    public OrderEntity saveOrder(OrderSubmitVO submitVO) {
        // 保存orderEntity
        MemberReceiveAddressEntity address = submitVO.getAddress();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverCity(address.getCity());
        Resp<MemberEntity> memberEntityResp = this.umsClient.queryMemberById(submitVO.getUserId());
        MemberEntity memberEntity = memberEntityResp.getData();
        orderEntity.setMemberUsername(memberEntity.getUsername());
//        orderEntity.setMemberId(submitVO.getUserId());

        // 清算每个商品赠送积分
        orderEntity.setIntegration(0);
        orderEntity.setGrowth(0);
        orderEntity.setDeleteStatus(0);
        orderEntity.setStatus(0);
        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(orderEntity.getCreateTime());
        orderEntity.setDeliveryCompany(submitVO.getDeliveryCompany());
        orderEntity.setSourceType(1);
        orderEntity.setPayType(submitVO.getPayType());
        orderEntity.setTotalAmount(submitVO.getTotalPrice());
        orderEntity.setOrderSn(submitVO.getOrderToken());
        this.save(orderEntity);

        Long orderId = orderEntity.getId();
        // 保存订单详情OrderItemEntity
        List<OrderItemVO> items = submitVO.getItems();
        items.forEach(item -> {
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setSkuId(item.getSkuId());
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();

            Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsClient.querySpuById(skuInfoEntity.getSpuId());
            SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();

            itemEntity.setSkuPrice(skuInfoEntity.getPrice());
            itemEntity.setSkuAttrsVals(JSON.toJSONString(item.getSaleAttrValues()));
            itemEntity.setCategoryId(skuInfoEntity.getCatalogId());
            itemEntity.setOrderId(orderId);
            itemEntity.setOrderSn(submitVO.getOrderToken());
            itemEntity.setSpuId(spuInfoEntity.getId());
            itemEntity.setSkuName(skuInfoEntity.getSkuName());
            itemEntity.setSkuPic(skuInfoEntity.getSkuDefaultImg());
            itemEntity.setSkuQuantity(item.getCount());
            itemEntity.setSpuName(spuInfoEntity.getSpuName());
            this.itemMapper.insert(itemEntity);
        });

        // 发送消息给延时队列，定时关单
        this.amqpTemplate.convertAndSend("SHOP-ORDER-EXCHANGE", "order.ttl", submitVO.getOrderToken());

        return orderEntity;
    }
}
