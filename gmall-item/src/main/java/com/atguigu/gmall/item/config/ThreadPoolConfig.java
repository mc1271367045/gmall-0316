package com.atguigu.gmall.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/04/13:21
 * @Description:
 */
@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${threadPool.coreSize}")Integer coreSize,
            @Value("${threadPool.maxSize}")Integer maxSize,
            @Value("${threadPool.timeOut}")Integer timeOut,
            @Value("${threadPool.blockingSize}")Integer blockingSize
    ){
        return new ThreadPoolExecutor(coreSize, maxSize, timeOut, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(blockingSize));
    }
}

