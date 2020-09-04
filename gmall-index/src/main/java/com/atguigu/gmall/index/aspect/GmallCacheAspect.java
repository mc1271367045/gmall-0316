package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取目标对象类：joinPoint.getTarget().getClass()
     * 获取方法签名：(MethodSignature)joinPoint.getSignature()
     * 获取目标方法参数：joinPoint.getArgs()
     * 获取目标方法：(MethodSignature)joinPoint.getSignature().getMethod()
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.atguigu.gmall.index.aspect.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取目标方法
        Method method = signature.getMethod();
        // 获取目标方法上的注解
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        // 获取注解中的前缀
        String prefix = gmallCache.prefix();
        // 获取目标方法的形参，数组的tostring方法返回的地址
        Object[] args = joinPoint.getArgs();
        // prefix + args 组成缓存key
        String key = prefix + Arrays.asList(args);
        // 获取方法的返回值类型
        Class returnType = signature.getReturnType();

        // 1.查询缓存，缓存中有，直接反序列化并返回
        String json = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json, returnType);
        }

        // 2.加分布式锁，防止缓存击穿
        String lock = gmallCache.lock();// 获取注解中锁的名称
        RLock fairLock = this.redissonClient.getFairLock(lock + Arrays.asList(args));// 只锁当前参数所对应的锁，提高性能
        fairLock.lock();

        // 3.再查缓存，有直接返回
        String json2 = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json2)){
            fairLock.unlock();// 切记：return之前要释放锁
            return JSON.parseObject(json2, returnType);
        }

        // 4.再去执行目标方法
        Object result = joinPoint.proceed(joinPoint.getArgs());

        // 5.把目标方法的返回值放入缓存
        int timeout = gmallCache.timeout();
        int random = gmallCache.random();
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout + new Random().nextInt(random), TimeUnit.MINUTES);

        // 6.解锁
        fairLock.unlock();

        // 7.返回结果
        return result;
    }
}
