package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/24/9:24
 * @Description:
 */
@Data
public class SpuVo extends SpuEntity {

    // 海报信息
    private List<String> spuImages;

    private List<BaseAttrValueVo> baseAttrs;

    private List<SkuVo> skus;

}
