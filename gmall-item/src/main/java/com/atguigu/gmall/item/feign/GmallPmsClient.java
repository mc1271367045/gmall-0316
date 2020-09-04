package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/25/21:10
 * @Description:
 */

@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
