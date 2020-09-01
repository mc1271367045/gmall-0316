package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/01/16:06
 * @Description:
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}