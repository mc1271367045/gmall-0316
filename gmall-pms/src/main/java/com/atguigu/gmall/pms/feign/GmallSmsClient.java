package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/24/14:23
 * @Description:
 */

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

//    @PostMapping("sms/skubounds/sales/save")
//    public ResponseVo<Object> saveSkuSales(@RequestBody SkuSaleVo skuSaleVo);

}
