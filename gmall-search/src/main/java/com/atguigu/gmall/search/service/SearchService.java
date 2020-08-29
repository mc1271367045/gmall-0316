package com.atguigu.gmall.search.service;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/29/9:28
 * @Description:
 */

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.aspectj.weaver.ast.Var;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import springfox.documentation.service.ApiListing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    // json转换
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 搜索方法
    public SearchResponseVo search(SearchParamVo paramVo) {

        try {
            SearchSourceBuilder sourceBuilder = buildDSL(paramVo);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices("goods");
            searchRequest.source(sourceBuilder);
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            SearchResponseVo responseVo = this.parseResult(response);
            // 通过查询条件来获得分页信息
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());
            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    // 解析DSL中数据
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo responseVo = new SearchResponseVo();

        // 解析搜索数据中的hits
        SearchHits hits = response.getHits();
        // 1 总记录数 ( 2当前页码 和 3每页记录数 用paramVo设置了)
        responseVo.setTotal(hits.getTotalHits());

        // 4 获取当前页数据
        SearchHit[] hitsHits = hits.getHits();
        if (hitsHits == null || hitsHits.length == 0){
            return responseVo;
        }
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            try {
                // 获取命中结果集中的_source
                String json = hitsHit.getSourceAsString();
                // 把_source反序列化为Goods对象
                Goods goods = MAPPER.readValue(json, Goods.class);

                // 把_source中的普通的Title 替换成 高亮结果集中title
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                if (!CollectionUtils.isEmpty(highlightFields)) {
                    HighlightField highlightField = highlightFields.get("title");
                    if (highlightField != null) {
                        Text[] fragments = highlightField.getFragments();
                        if (fragments != null && fragments.length > 0){
                            String title = fragments[0].string();
                            goods.setTitle(title);
                        }
                    }
                }
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);


        // 5 解析搜索数据中的聚合数据
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

        // 5.1 获取品牌聚合，解析出品牌集合
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        List<BrandEntity> brandEntities = buckets.stream().map(bucket -> {
            BrandEntity brandEntity = new BrandEntity();

            // 解析出桶的品牌id
            Long brandId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            brandEntity.setId(brandId);
            // 获取桶中的子聚合
            Map<String, Aggregation> subAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();

            // 获取品牌名称的子聚合
            ParsedStringTerms brandNameAgg = (ParsedStringTerms)subAggregationMap.get("brandNameAgg");
            List<? extends Terms.Bucket> nameBuckets = brandNameAgg.getBuckets();
            if (!CollectionUtils.isEmpty(nameBuckets)){
                Terms.Bucket nameBucket = nameBuckets.get(0);
                if (nameBucket != null) {
                    brandEntity.setName(nameBucket.getKeyAsString());
                }
            }

            // 获取logo的子聚合
            ParsedStringTerms brandLogoAgg = (ParsedStringTerms)subAggregationMap.get("brandLogoAgg");
            if (brandLogoAgg != null) {
                List<? extends Terms.Bucket> logoBuckets = brandLogoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoBuckets)){
                    Terms.Bucket logoBucket = logoBuckets.get(0);
                    if (logoBucket != null) {
                        brandEntity.setLogo(logoBucket.getKeyAsString());
                    }
                }
            }

            return brandEntity;
        }).collect(Collectors.toList());
        responseVo.setBrands(brandEntities);

        // 5.2 解析分类id聚合获取分类的过滤条件
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        List<CategoryEntity> categoryEntities = categoryBuckets.stream().map(bucket -> {
            CategoryEntity categoryEntity = new CategoryEntity();

            Long categoryId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
            categoryEntity.setId(categoryId);

            // 获取分类名称的子聚合
            ParsedStringTerms categoryNameAgg = (ParsedStringTerms)((Terms.Bucket) bucket).getAggregations().get("categoryNameAgg");
            if (categoryNameAgg != null) {
                List<? extends Terms.Bucket> nameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)){
                    categoryEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
            }
            return categoryEntity;
        }).collect(Collectors.toList());
        responseVo.setCategories(categoryEntities);


        // 5.3 获取规格参数的嵌套聚合
        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");
        // 获取嵌套聚合中的attrIdAgg子聚合
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        if (attrIdAgg != null){
            // 获取聚合中所有的桶
            List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
            if (!CollectionUtils.isEmpty(attrIdAggBuckets)){
                // 每个桶转化成每个对应SearchResponseAttrVo
                List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAggBuckets.stream().map(bucket -> {
                    SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();

                    // 解析桶中的key获取规格参数的id
                    Long attrId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                    searchResponseAttrVo.setAttrId(attrId);

                    // 获取桶中所有的子聚合
                    Map<String, Aggregation> subAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();
                    // 获取规格参数名的子聚合
                    ParsedStringTerms attrNameAgg = (ParsedStringTerms)subAggregationMap.get("attrNameAgg");
                    if (attrNameAgg != null) {
                        List<? extends Terms.Bucket> nameAggBuckets = attrNameAgg.getBuckets();
                        if (!CollectionUtils.isEmpty(nameAggBuckets)){
                            Terms.Bucket nameBucket = nameAggBuckets.get(0);
                            if (nameBucket != null) {
                                searchResponseAttrVo.setAttrName(nameBucket.getKeyAsString());
                            }
                        }
                    }

                    // 获取规格参数值的子聚合
                    ParsedStringTerms attrValueAgg = (ParsedStringTerms)subAggregationMap.get("attrValueAgg");
                    if (attrValueAgg != null) {
                        List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                        if (!CollectionUtils.isEmpty(attrValueAggBuckets)){
                            searchResponseAttrVo.setAttrValues(attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                        }
                    }

                    return searchResponseAttrVo;
                }).collect(Collectors.toList());
                responseVo.setFilters(searchResponseAttrVos);
            }
        }

        return responseVo;
    }

    // 获取DSL中数据
    private SearchSourceBuilder buildDSL(SearchParamVo paramVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        String keyword = paramVo.getKeyword();
        if(StringUtils.isBlank(keyword)){
            // TODO 打广告
            return sourceBuilder;
        }
        // 1.构建查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);

        // 1.1 匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));

        // 1.2 品牌过滤
        List<Long> brandId = paramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }

        // 1.3 分类过滤
        List<Long> cid = paramVo.getCid();
        if (!CollectionUtils.isEmpty(cid)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", cid));
        }

        // 1.4 规格参数过滤
        // props=4:6G-8G-12G&props=5:128G-256G-512G
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)){
            props.forEach(prop -> { // 每一个prop:  8:8G-12G
                // 先以：分割获取规格参数id 以及8G-12G
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2){
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));
                    // 再以-分割获取规格参数值数组
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    if (attrValues != null && attrValues.length > 0){
                        boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    }
                    NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None);
                    boolQueryBuilder.filter(nestedQuery);
                }
            });
        }

        // 1.5 价格区间
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        // 如果价格区间都为空 则不加入
        if (priceFrom != null || priceTo != null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            boolQueryBuilder.filter(rangeQuery);
            if (priceFrom != null){
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null){
                rangeQuery.lte(priceTo);
            }
        }

        // 1.6 是否有货
        Boolean store = paramVo.getStore();
        if (store != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }

        // 2 构建排序 0-得分排序 1-价格升序 2-价格降序 3-新品降序 4-销量降序
        Integer sort = paramVo.getSort();
        if(sort == null){
            sort = 0 ;
        }
        switch (sort){
            case 1: sourceBuilder.sort("price", SortOrder.ASC); break;
            case 2: sourceBuilder.sort("price", SortOrder.DESC); break;
            case 3: sourceBuilder.sort("createTime", SortOrder.DESC); break;
            case 4: sourceBuilder.sort("sales", SortOrder.DESC); break;
            default:
                sourceBuilder.sort("_score", SortOrder.ASC);
                break;
        }

        // 3 构建分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum-1) * pageSize);
        sourceBuilder.size(pageSize);

        // 4 高亮
        sourceBuilder.highlighter(new HighlightBuilder()
                .field("title")
                .preTags("<font style='color:red;'>")
                .postTags("</font>"));

        // 5 构建聚合
        // 5.1 构建品牌聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );

        // 5.2 构建分类聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );

        // 5.3 构建规格参数聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                )
        );

        // 6 指定包含的字段
        sourceBuilder.fetchSource(new String[]{"skuId", "title", "price", "image", "subTitle"}, null);

        System.out.println("sourceBuilder = "+ sourceBuilder.toString());
        return sourceBuilder;
    }
}
