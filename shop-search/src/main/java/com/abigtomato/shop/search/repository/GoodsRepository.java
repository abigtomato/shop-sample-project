package com.abigtomato.shop.search.repository;

import com.abigtomato.shop.search.pojo.GoodsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsRepository extends ElasticsearchRepository<GoodsDoc, Long> {
}
