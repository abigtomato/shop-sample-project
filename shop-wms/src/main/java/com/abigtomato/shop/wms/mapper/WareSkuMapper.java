package com.abigtomato.shop.wms.mapper;

import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {

    @Select(value = "SELECT * FROM wms_ware_sku WHERE sku_id = #{skuId} AND stock - stock_locked >= #{count}")
    List<WareSkuEntity> checkStore(@Param("skuId") Long skuId, @Param("count") Integer count);

    @Update(value = "UPDATE wms_ware_sku SET stock_locked = stock_locked + #{count} WHERE id = #{id}")
    void lockStore(@Param("id") Long id, @Param("count") Integer count);

    @Update(value = "UPDATE wms_ware_sku SET stock_locked = stock_locked - #{count} WHERE id = #{wareSkuId}")
    void unLockStore(@Param("wareSkuId") Long wareSkuId, @Param("count") Integer count);

    @Update(value = "UPDATE wms_ware_sku SET stock_locked = stock_locked - #{count}, stock = stock - #{count} WHERE id = #{wareSkuId}")
    void minusStore(@Param("wareSkuId") Long wareSkuId, @Param("count") Integer count);
}
