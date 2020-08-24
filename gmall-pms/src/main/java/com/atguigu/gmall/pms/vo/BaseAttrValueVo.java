package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/24/9:25
 * @Description:
 */
public class BaseAttrValueVo extends SpuAttrValueEntity {

    //由于规格参数返回的与实体类不一致，使用set方法修改attrvalue为valueSelected
    public void setValueSelected(List<String> valueSelected){
        if (!CollectionUtils.isEmpty(valueSelected)){
            this.setAttrValue(StringUtils.join(valueSelected, ","));
        }
    }
}
