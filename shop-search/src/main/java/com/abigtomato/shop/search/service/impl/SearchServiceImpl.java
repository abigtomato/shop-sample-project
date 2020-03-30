package com.abigtomato.shop.search.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.abigtomato.shop.api.pms.entity.*;
import com.abigtomato.shop.api.wms.entity.WareSkuEntity;
import com.abigtomato.shop.core.bean.QueryCondition;
import com.abigtomato.shop.core.bean.Resp;
import com.abigtomato.shop.search.feign.ShopPmsClient;
import com.abigtomato.shop.search.feign.ShopWmsClient;
import com.abigtomato.shop.search.pojo.*;
import com.abigtomato.shop.search.repository.GoodsRepository;
import com.abigtomato.shop.search.service.SearchService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    private ElasticsearchRestTemplate restTemplate;

    private GoodsRepository goodsRepository;

    private RestHighLevelClient restHighLevelClient;

    private ShopPmsClient shopPmsClient;

    private ShopWmsClient shopWmsClient;

    @Autowired
    public SearchServiceImpl(ElasticsearchRestTemplate restTemplate,
                             GoodsRepository goodsRepository,
                             RestHighLevelClient restHighLevelClient,
                             ShopPmsClient shopPmsClient,
                             ShopWmsClient shopWmsClient) {
        this.restTemplate = restTemplate;
        this.goodsRepository = goodsRepository;
        this.restHighLevelClient = restHighLevelClient;
        this.shopPmsClient = shopPmsClient;
        this.shopWmsClient = shopWmsClient;
    }

    @Override
    public void create() {
        // 创建索引库
        this.restTemplate.createIndex(GoodsDoc.class);
        // 创建映射
        this.restTemplate.putMapping(GoodsDoc.class);
    }

    @Override
    public void importData() {
        long pageNum = 1L;
        long pageSize = 100L;

        do {
            // 查询spu
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> spuResp = this.shopPmsClient.querySpusByPage(queryCondition);
            List<SpuInfoEntity> spus = spuResp.getData();

            if (CollUtil.isEmpty(spus)) {
                return;
            }

            spus.forEach(spuInfoEntity -> {
                // 根据spu查询sku
                Resp<List<SkuInfoEntity>> skuResp = this.shopPmsClient.querySkusBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();

                if (CollUtil.isNotEmpty(skuInfoEntities)) {
                    // 将sku转换为goods
                    List<GoodsDoc> goodsDocs = skuInfoEntities.stream().map(skuInfoEntity -> {
                        GoodsDoc goodsDoc = new GoodsDoc();

                        // 填充属性相关字段
                        Resp<List<ProductAttrValueEntity>> attrValueResp = this.shopPmsClient.querySearchAttrValueBySpuId(spuInfoEntity.getId());
                        List<ProductAttrValueEntity> attrValueEntities = attrValueResp.getData();
                        if (CollUtil.isNotEmpty(attrValueEntities)) {
                            List<GoodsAttr> goodsAttrs = attrValueEntities.stream().map(productAttrValueEntity -> {
                                GoodsAttr goodsAttr = new GoodsAttr();
                                goodsAttr.setAttrId(productAttrValueEntity.getAttrId());
                                goodsAttr.setAttrName(productAttrValueEntity.getAttrName());
                                goodsAttr.setAttrValue(productAttrValueEntity.getAttrValue());
                                return goodsAttr;
                            }).collect(Collectors.toList());
                            goodsDoc.setAttrs(goodsAttrs);
                        }

                        // 填充品牌相关字段
                        Resp<BrandEntity> brandEntityResp = this.shopPmsClient.queryBrandById(skuInfoEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResp.getData();
                        if (brandEntity != null) {
                            goodsDoc.setBrandId(brandEntity.getBrandId());
                            goodsDoc.setBrandName(brandEntity.getName());
                        }

                        // 填充分类相关字段
                        Resp<CategoryEntity> categoryEntityResp = this.shopPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                        CategoryEntity categoryEntity = categoryEntityResp.getData();
                        if (categoryEntity != null) {
                            goodsDoc.setCategoryId(skuInfoEntity.getCatalogId());
                            goodsDoc.setCategoryName(categoryEntity.getName());
                        }

                        // 填充库存相关字段
                        Resp<List<WareSkuEntity>> listResp = this.shopWmsClient.queryWareSkusBySkuId(skuInfoEntity.getSkuId());
                        List<WareSkuEntity> wareSkuEntities = listResp.getData();
                        if (CollUtil.isNotEmpty(wareSkuEntities)) {
                            // anyMatch 表示流中的元素有任何一个满足条件则返回true，全不满足则返回false
                            boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                            goodsDoc.setStore(flag);
                        }

                        // 填充其他字段
                        goodsDoc.setCreateTime(spuInfoEntity.getCreateTime());
                        goodsDoc.setPic(skuInfoEntity.getSkuDefaultImg());
                        goodsDoc.setPrice(skuInfoEntity.getPrice().doubleValue());
                        goodsDoc.setSale(0L);
                        goodsDoc.setSkuId(skuInfoEntity.getSkuId());
                        goodsDoc.setTitle(skuInfoEntity.getSkuTitle());
                        return goodsDoc;
                    }).collect(Collectors.toList());

                    // 导入索引库
                    this.goodsRepository.saveAll(goodsDocs);
                }
            });

            pageSize = spus.size();
            pageNum++;
        } while (pageSize == 100);
    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        SearchResponseVo responseVo = null;

        // 构建DSL语句
        Optional<SearchRequest> optional = this.buildQueryDSL(searchParam);
        if (optional.isPresent()) {
            SearchResponse response = this.restHighLevelClient.search(optional.get(), RequestOptions.DEFAULT);
            // 解析搜索结果
            responseVo = this.parseSearchResult(response, searchParam.getPageSize(), searchParam.getPageNum());
        }
        return responseVo;
    }

    private Optional<SearchRequest> buildQueryDSL(SearchParam searchParam) {
        String keyword = searchParam.getKeyword();
        if (StrUtil.isEmpty(keyword)) {
            return Optional.empty();
        }

        // 最外层的查询条件构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // bool查询构建器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 关键字匹配（分词，倒排索引）
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));

        // 多品牌过滤（词条匹配）
        String[] brand = searchParam.getBrand();
        if (ArrayUtil.isNotEmpty(brand)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brand));
        }

        // 多分类过滤
        String[] catelog3 = searchParam.getCatelog3();
        if (ArrayUtil.isNotEmpty(catelog3)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", catelog3));
        }

        // 规格属性的嵌套过滤
        String[] props = searchParam.getProps();
        if (ArrayUtil.isNotEmpty(props)) {
            Arrays.stream(props).forEach(prop -> {
                // prop参数格式：
                // attrId:attrValue1-attrValue2-attrValue3-attrValue4...
                String[] split = StrUtil.split(":", prop);
                if (split != null && split.length == 2) {
                    String[] attrValues = StrUtil.split("-", split[1]);

                    // 嵌套查询中的子查询
                    BoolQueryBuilder nestedSubBoolQuery = QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("attrs.attrId", split[0]))
                            .must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                    // 嵌套查询（处理嵌套字段的查询，attrs就是嵌套字段，包含attrId，attrName，attrValue这样的子字段）
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                            .must(QueryBuilders.nestedQuery("attrs", nestedSubBoolQuery, ScoreMode.None));

                    boolQueryBuilder.filter(boolQuery);
                }
            });
        }

        // 价格区间过滤
        Integer priceFrom = searchParam.getPriceFrom();
        Integer priceTo = searchParam.getPriceTo();
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
        if (priceFrom != null) {
            rangeQueryBuilder.gte(priceFrom);
        }
        if (priceTo != null) {
            rangeQueryBuilder.lte(priceTo);
        }
        boolQueryBuilder.filter(rangeQueryBuilder);

        // 将bool查询构建器嵌入最外层的构建器中（此时包含匹配和过滤）
        sourceBuilder.query(boolQueryBuilder);

        // 分页
        Integer pageNum = searchParam.getPageNum();
        Integer pageSize = searchParam.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        // 排序
        String order = searchParam.getOrder();
        if (StrUtil.isNotEmpty(order)) {
            String[] split = StrUtil.split(":", order);
            if (split != null && split.length == 2) {
                sourceBuilder.sort(StrUtil.equals(split[0], "1") ? "sale" : "price",
                        StrUtil.equals(split[1], "asc") ? SortOrder.ASC : SortOrder.DESC);
            }
        }

        // 高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<em>").postTags("</em>"));

        // 根据品牌id聚合（就是根据brandId字段分组）
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                // 对聚合后的每个分组做聚合（按照brandName再分组）
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));

        // 对分类进行聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        // 构建嵌套聚合（也就是处理嵌套字段的聚合）
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrs")
                // 嵌套聚合的子聚合，根据attrs.attrId分组
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        // 针对父聚合后的每个分组按照attrs.attrName再次分组
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        // 针对父聚合后的每个分组按照attrs.attrValue再次分组
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")));
        sourceBuilder.aggregation(nestedAggregationBuilder);

        // 结果集过滤，includes表示需要展示的字段，excludes表示需要不需要展示的字段
        sourceBuilder.fetchSource(new String[]{"skuId", "pic", "title", "price"}, null);

        // 构建搜索请求对象，指定索引库，类型和构建器
        SearchRequest request = new SearchRequest("goods").types("info").source(sourceBuilder);
        return Optional.of(request);
    }

    private SearchResponseVo parseSearchResult(SearchResponse response, Integer pageSize, Integer pageNum) {
        SearchResponseVo responseVo = new SearchResponseVo();
        SearchHits hits = response.getHits();
        // 总记录数
        responseVo.setTotal(hits.totalHits);

        // 获取聚合操作的结果集
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

        // 获取品牌id聚合的结果集
        List<String> brandValues = ((ParsedLongTerms) aggregationMap.get("brandIdAgg")).getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", bucket.getKeyAsString());

            // 获取品牌名称子聚合的结果集
            Map<String, Aggregation> brandIdAggSubMap = bucket.getAggregations().asMap();
            String brandName = ((ParsedStringTerms) brandIdAggSubMap.get("brandNameAgg")).getBuckets().get(0).getKeyAsString();
            map.put("name", brandName);

            return JSON.toJSONString(map);
        }).collect(Collectors.toList());
        SearchResponseAttrVo brand = new SearchResponseAttrVo();
        brand.setName("品牌");
        brand.setValue(brandValues);
        responseVo.setBrand(brand);

        // 获取分类id聚合的结果集
        List<String> categoryValues = ((ParsedLongTerms) aggregationMap.get("categoryIdAgg")).getBuckets().stream().map(bucket -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", bucket.getKeyAsString());

            // 获取分类名称子聚合的结果集
            Map<String, Aggregation> categoryIdAggSubMap = bucket.getAggregations().asMap();
            String categoryName = ((ParsedStringTerms) categoryIdAggSubMap.get("categoryNameAgg")).getBuckets().get(0).getKeyAsString();
            map.put("name", categoryName);

            return JSON.toJSONString(map);
        }).collect(Collectors.toList());
        SearchResponseAttrVo category = new SearchResponseAttrVo();
        category.setName("分类");
        category.setValue(categoryValues);
        responseVo.setCatelog(category);

        // 解析（json）并转换（map）搜索结果集
        List<GoodsDoc> goodsList = new ArrayList<>();
        Arrays.stream(hits.getHits()).forEach(hit -> {
            GoodsDoc goodsDoc = JSON.parseObject(hit.getSourceAsString(), GoodsDoc.class);
            goodsDoc.setTitle(hit.getHighlightFields().get("title").getFragments()[0].toString());
            goodsList.add(goodsDoc);
        });
        responseVo.setProducts(goodsList);

        // 获取根据嵌套聚合的结果集（根据attrs的聚合）
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        // 获取嵌套聚合的子聚合结果集（根据attrs.attrId的聚合）
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (CollUtil.isNotEmpty(buckets)) {
            List<SearchResponseAttrVo> attrs = buckets.stream().map(bucket -> {
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();

                // 规格参数id
                responseAttrVo.setProductAttributeId(bucket.getKeyAsNumber().longValue());

                // 获取第二层子聚合的结果集（根据attrs.attrName的聚合）
                List<? extends Terms.Bucket> nameBuckets = ((ParsedStringTerms) bucket.getAggregations().get("attrNameAgg")).getBuckets();
                // 规格参数名
                responseAttrVo.setName(nameBuckets.get(0).getKeyAsString());

                // 获取第二层子聚合的结果集（根据attrs.attrValue的聚合）
                List<? extends Terms.Bucket> valueBuckets = ((ParsedStringTerms) bucket.getAggregations().get("attrValueAgg")).getBuckets();
                // 规格参数值的列表
                List<String> values = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                responseAttrVo.setValue(values);

                return responseAttrVo;
            }).collect(Collectors.toList());
            responseVo.setAttrs(attrs);
        }

        return responseVo;
    }
}
