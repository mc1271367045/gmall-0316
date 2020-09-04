package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品spu积分设置
 *
 * @author è¿æ ·Ba
 * @email 1271367045@qq.com
 * @date 2020-08-21 12:59:08
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    // 保存营销信息（三张表）
    void saveSkuSales(SkuSaleVo skuSaleVo);


    // 根据skuid查询营销信息
    List<ItemSaleVo> querySalesBySkuId(Long skuId);
}

