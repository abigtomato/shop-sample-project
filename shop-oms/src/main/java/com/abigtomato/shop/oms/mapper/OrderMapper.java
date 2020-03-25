package com.abigtomato.shop.oms.mapper;

import com.abigtomato.shop.api.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface OrderMapper extends BaseMapper<OrderEntity> {

    @Update(value = "UPDATE oms_order SET `status` = 4 WHERE order_sn = #{orderToken} AND `status` = 0")
    int closeOrder(@Param("orderToken") String orderToken);
}
