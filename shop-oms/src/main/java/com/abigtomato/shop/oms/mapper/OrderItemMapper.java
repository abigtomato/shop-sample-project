package com.abigtomato.shop.oms.mapper;

import com.abigtomato.shop.api.oms.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface OrderItemMapper extends BaseMapper<OrderItemEntity> {
}
