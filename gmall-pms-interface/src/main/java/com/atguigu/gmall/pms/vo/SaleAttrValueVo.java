package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/25/18:49
 * @Description:
 */
@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;
    private Set<String> attrValues;


}
