package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * sku图片
 * 
 * @author è¿æ ·Ba
 * @email 1271367045@qq.com
 * @date 2020-08-21 11:18:33
 */
@Data
@TableName("pms_sku_images")
public class SkuImagesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * 图片地址
	 */
	private String url;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 默认图[0 - 不是默认图，1 - 是默认图]
	 */
	private Integer defaultStatus;

}
