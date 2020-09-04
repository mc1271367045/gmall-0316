package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/28/14:03
 * @Description:
 */
public interface GmallPmsApi {

    // 分页查询spu
    @PostMapping("pms/spu/json")
    ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    // 根据spuId查询spu下的sku
    @GetMapping("pms/sku/spu/{spuId}")
    ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId")Long spuId);

    // 根据品牌的id查询品牌
    @GetMapping("pms/brand/{id}")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    // 根据分类的id查询分类
    @GetMapping("pms/category/{id}")
    ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    // 根据skuid查询销售类型并且是搜索类型的参数和值
    @GetMapping("pms/skuattrvalue/search/{skuId}")
    ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(@PathVariable("skuId")Long skuId);

    // 根据spuid查询基本类型并且是搜索类型的参数和值
    @GetMapping("pms/spuattrvalue/search/{spuId}")
    ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/spu/{id}")
    ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/category/parent/{parentId}")
    ResponseVo<List<CategoryEntity>> queryCategoriesByPid(@PathVariable("parentId")Long pid);

    @GetMapping("pms/category/cates/{pid}")
    ResponseVo<List<CategoryEntity>> queryCategoriesWithSubByPid(@PathVariable("pid")Long pid);

    // 根据id查询sku信息
    @GetMapping("pms/sku/{id}")
    ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    // 根据id查询spu的描述信息
    @GetMapping("pms/spudesc/{spuId}")
    ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    // 根据三级分类的id查询一二三及分类
    @GetMapping("pms/category/all/{cid}")
    public ResponseVo<List<CategoryEntity>> query123CategoriesByCid3(@PathVariable("cid")Long cid);

    // 根据skuId查询sku的图片
    @GetMapping("pms/skuimages/sku/{skuId}")
    ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId")Long skuId);

    // 根据spuid查询所有销售属性的取值
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> queryAllSaleAttrValueBySpuId(@PathVariable("spuId")Long spuId);

    // 根据skuid查询该sku的销售属性
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValueBySkuId(@PathVariable("skuId")Long skuId);

    // 根据spuid查询所有销售属性的取值
    @GetMapping("pms/skuattrvalue/sku/spu/{spuId}")
    public ResponseVo<String> querySaleAttrMappingSkuIdBySpuId(@PathVariable("spuId")Long spuId);

    // 查询组及组下参数和值
    @GetMapping("pms/attrgroup/withattr/withvalue/{cid}")
    public ResponseVo<List<ItemGroupVo>> queryGroupWithAttrValue(@PathVariable("cid")Long cid,
                                                                 @RequestParam("spuId") Long spuId,
                                                                 @RequestParam("skuId")Long skuId);


}
