package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/28/13:49
 * @Description:
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {

}
