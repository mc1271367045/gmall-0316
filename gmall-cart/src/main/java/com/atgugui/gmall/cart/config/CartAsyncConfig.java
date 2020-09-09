package com.atgugui.gmall.cart.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/30/11:29
 * @Description:
 */
@Configuration
public class CartAsyncConfig implements AsyncConfigurer {


    @Autowired
    private CartAsyncExceptionHandler cartAsyncExceptionHandler;

    /**
     * 给springTask定义专有线程池 这次采用在yml文件中配置的方式来实现
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    /**
     * 注册统一异常处理器
     * @return
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return cartAsyncExceptionHandler;
    }
}
