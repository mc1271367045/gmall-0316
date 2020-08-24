package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/24/14:44
 * @Description:
 */
public interface GmallSmsApi {

    @PostMapping("sms/skubounds/sales/save")
    ResponseVo<Object> saveSkuSales(@RequestBody SkuSaleVo skuSaleVo);
}
