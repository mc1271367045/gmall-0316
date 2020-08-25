package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.BaseAttrValueVo;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    // 查询spu列表
    @Override
    public PageResultVo querySpuByPageAndCid3(Long cid, PageParamVo paramVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();

        // 判断cid是否为0，不为0查询指定分类
        if (cid != 0){
            wrapper.eq("category_id", cid);
        }

        // 判断key不为空要有关键字查询
        String key = paramVo.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.like("name", key).or().eq("id", key));
        }

        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }

//    @Autowired
//    private SpuDescMapper descMapper;

    @Autowired
    private SpuDescService spuDescService;


    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SkuAttrValueService attrValueService;

    @Autowired
    private GmallSmsClient smsClient;

    // 大保存方法（9张表）
    @GlobalTransactional
    // @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void bigSave(SpuVo spuVo) {

        // 1.保存spu的三张表
        // 1.1 保存pms_spu
        Long spuId = saveSpu(spuVo); // 保存失败

        // 1.2 保存pms_spu_desc
        this.spuDescService.saveSpuDesc(spuVo, spuId); // 保存成功

        // 制造异常
        // int i = 1/0;

        // 1.3 保存pms_spu_attr_value
        saveBaseAttr(spuVo, spuId);

        // 2.保存sku的三张表
        saveSku(spuVo, spuId);
    }

    private void saveSku(SpuVo spuVo, Long spuId) {
        List<SkuVo> skus = spuVo.getSkus();
        if(CollectionUtils.isEmpty(skus)){
            return;
        }

        // 遍历保存sku的相关信息
        skus.forEach(skuVo -> {
            // 2.1 保存pms_sku
            skuVo.setId(null);
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(spuVo.getBrandId());
            skuVo.setCatagoryId(spuVo.getCategoryId());
            List<String> images = skuVo.getImages();
            // 如果页面没有传递默认图片取第一张图片作为默认图片
            if (!CollectionUtils.isEmpty(images)){
                skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage() : images.get(0));
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();

            // 2.2 保存pms_sku_Images
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setId(null);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setSort(1);
                    // 设置图片的默认状态，通过图片的地址是否为默认图片地址即可
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(image, skuVo.getDefaultImage()) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.imagesService.saveBatch(skuImagesEntities);
            }

            // 2.3 保存pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(saleAttr -> {
                    saleAttr.setSkuId(skuId);
                    saleAttr.setId(null);
                    saleAttr.setSort(0);
                });
                this.attrValueService.saveBatch(saleAttrs);
            }

            // 3.保存sms优惠信息的三张表(远程调用)
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.smsClient.saveSkuSales(skuSaleVo);

            // 制造异常
            // int i = 1/0;

        });
    }

    private void saveBaseAttr(SpuVo spuVo, Long spuId) {
        List<BaseAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(baseAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(baseAttrValueVo,spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                spuAttrValueEntity.setSort(1);
                spuAttrValueEntity.setId(null);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }



    private Long saveSpu(SpuVo spuVo) {
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        spuVo.setId(null);// 防止id注入，显示的设置id为null
        this.save(spuVo);
        return spuVo.getId();
    }

}