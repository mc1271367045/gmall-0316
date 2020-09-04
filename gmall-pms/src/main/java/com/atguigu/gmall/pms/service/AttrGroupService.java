package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author è¿æ ·Ba
 * @email 1271367045@qq.com
 * @date 2020-08-21 11:18:33
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryGroupsWithAttrsByCid(Long cid);

    // 根据分类id结合spuid和skuid查询组及组下的规格参数与值
    List<ItemGroupVo> queryGroupWithAttrValue(Long cid, Long spuId, Long skuId);

}


