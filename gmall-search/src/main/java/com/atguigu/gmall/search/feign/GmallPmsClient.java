package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/28/13:48
 * @Description:
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
