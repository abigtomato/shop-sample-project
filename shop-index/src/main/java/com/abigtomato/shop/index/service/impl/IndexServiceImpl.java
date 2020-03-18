package com.abigtomato.shop.index.service.impl;

import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.index.annotation.ShopCache;
import com.abigtomato.shop.index.feign.ShopPmsClient;
import com.abigtomato.shop.index.service.IndexService;
import com.abigtomato.shop.pms.entity.CategoryEntity;
import com.abigtomato.shop.pms.vo.CategoryVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class IndexServiceImpl implements IndexService {

    private ShopPmsClient shopPmsClient;

    @Autowired
    public IndexServiceImpl(ShopPmsClient shopPmsClient) {
        this.shopPmsClient = shopPmsClient;
    }

    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        Resp<List<CategoryEntity>> listResp = this.shopPmsClient.queryCategoriesByPidOrLevel(1);
        return listResp.getData();
    }

    @Override
    public List<CategoryVo> querySubCategories(Long pid) {
        Resp<List<CategoryVo>> listResp = this.shopPmsClient.querySubCategories(pid);
        return listResp.getData();
    }

    @Override
    @ShopCache(prefix = "index:cates", timeout = 7200, random = 100)    // 自定义缓存注解
    public List<CategoryVo> querySubCategoriesV2(Long pid) {
        Resp<List<CategoryVo>> listResp = this.shopPmsClient.querySubCategories(pid);
        return listResp.getData();
    }
}
