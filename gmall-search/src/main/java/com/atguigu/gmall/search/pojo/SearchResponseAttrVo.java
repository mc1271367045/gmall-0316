package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/29/9:26
 * @Description:
 */
@Data
public class SearchResponseAttrVo {

    // 规格参数的id
    private Long attrId;
    // 规格参数的名称
    private String attrName;
    // 规格参数的列表
    private List<String> attrValues;
}