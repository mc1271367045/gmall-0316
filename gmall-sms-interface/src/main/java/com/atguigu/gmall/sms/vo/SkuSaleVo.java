package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/24/14:13
 * @Description:
 */
@Data
public class SkuSaleVo {
    private Long skuId;

    // 积分相关信息的接收
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    // 打折相关信息的接收
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    // 满减相关信息的接收
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

}
