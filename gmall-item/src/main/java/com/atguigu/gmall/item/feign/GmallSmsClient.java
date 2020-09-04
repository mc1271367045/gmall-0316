package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/25/21:11
 * @Description:
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
