package com.atguigu.gmall.search.pojo;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/28/10:37
 * @Description:
 */

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * 每一个goods对应的是一个sku
 */
@Data
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
public class Goods {

    // 商品详情列表中需要的字段
    @Id
    //@Field(type = FieldType.Long)
    private Long skuId;
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;
    @Field(type = FieldType.Double)
    private Double price;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Keyword)
    private String subTitle;

    // 排序过滤字段
    @Field(type = FieldType.Integer)
    private Integer sales; // 销量
    @Field(type = FieldType.Date)
    private Date createTime; // 新品排序,创建时间
    @Field(type = FieldType.Boolean)
    private Boolean store = false; // 是否有货

    // 品牌过滤
    @Field(type = FieldType.Long)
    private Long brandId;
    @Field(type = FieldType.Keyword)
    private String brandName;
    @Field(type = FieldType.Keyword)
    private String logo;

    // 分类过滤
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String categoryName;

    // 规格参数
    @Field(type = FieldType.Nested)//嵌套类型 是为了防止数据扁平化
    private List<SearchAttrValue> searchAttrs;



}
