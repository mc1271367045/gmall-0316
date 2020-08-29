package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/29/9:03
 * @Description:
 * search.gmall.com/search?keyword=星星&brand=1,2,7,6,2&cid=225,250&props=4:6G-8G-12G&props=5:128G-256G-512G&store=false&priceFrom=1000&priceTo=8000&sort=2
 */
@Data
public class SearchParamVo {

    private String keyword; // 搜索关键字
    private List<Long> brandId; // 品牌过滤
    private List<Long> cid; // 分类排序
    private List<String> props; // 规格参数过滤

    private Integer sort;  // 0-得分排序 1-价格升序 2-价格降序 3-新品降序 4-销量降序

    // 价格区间过滤
    private Double priceFrom;
    private Double priceTo;

    // 页码过滤
    private Integer pageNum = 1;
    private final Integer pageSize = 20;

    private Boolean store; // 是否有货




}
