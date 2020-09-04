package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/25/21:11
 * @Description:
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
