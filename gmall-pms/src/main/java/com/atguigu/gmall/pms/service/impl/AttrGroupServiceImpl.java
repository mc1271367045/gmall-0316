package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    // 查询分类下的组以及参数
    @Override
    public List<AttrGroupEntity> queryGroupsWithAttrsByCid(Long cid) {
        // 1.根据cid查询分组
        List<AttrGroupEntity> groups = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        if (CollectionUtils.isEmpty(groups)) {
            return null;
        }

        groups.forEach(group ->{
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", group.getId()).eq("type", 1));
            group.setAttrEntities(attrEntities);
        });

        return groups;

    }

    // 根据分类id结合spuid和skuid查询组及组下的规格参数与值
    @Override
    public List<ItemGroupVo> queryGroupWithAttrValue(Long cid, Long spuId, Long skuId) {
        // 根据分类id查询所有分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));

        if (CollectionUtils.isEmpty(attrGroupEntities)){
            return null;
        }

        return attrGroupEntities.stream().map(attrGroupEntity -> {
            ItemGroupVo groupVo  = new ItemGroupVo();
            // 遍历所有组，查询组下的规格参数
            groupVo.setGroupId(attrGroupEntity.getId());
            groupVo.setGroupName(attrGroupEntity.getName());

            // 获取分组下的所有规格参数
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));
            if (CollectionUtils.isEmpty(attrEntities)){
                return groupVo;
            }
            List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

            List<AttrValueVo> attrs = new ArrayList<>();
            // 结合spuid查询通用的规格参数及值
            List<SpuAttrValueEntity> spuAttrValueEntities = this.spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));
            if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                attrs.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList()));
            }

            // 结合skuid查询销售的规格参数及值
            List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                attrs.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList()));
            }

            groupVo.setAttrs(attrs);
            return groupVo;
        }).collect(Collectors.toList());
    }

}