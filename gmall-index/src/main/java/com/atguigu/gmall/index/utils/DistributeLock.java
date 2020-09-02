package com.atguigu.gmall.index.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 此类封装Lua脚本 用于解决分布式锁的可重入与自动续期问题
 */
@Component
public class DistributeLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Thread thread;

    // 获取锁
    public Boolean tryLock(String lockName, String uuid, Long expire){
        String script = "if (redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1) " +
                "then redis.call('hincrby', KEYS[1], ARGV[1], 1); redis.call('expire', KEYS[1], ARGV[2]); return 1; " +
                "else return 0; end;";
        if(this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid, expire.toString()) == 0){
            // 如果获取锁失败，则重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tryLock(lockName, uuid, expire);
        }
        renewTime(lockName, uuid, expire);
        return true;
    }

    // 释放锁
    public void unLock(String lockName, String uuid){
        String script = "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then return nil end; " +
                "if (redis.call('hincrby', KEYS[1], ARGV[1], -1) > 0) then return 0 " +
                "else redis.call('del', KEYS[1]) return 1 end;";
        if(this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid) == null){
            throw new IllegalArgumentException("您在尝试解除别人的锁, lockname: " + lockName + ", uuid: " + uuid);
        }
        this.thread.interrupt(); // 中断线程
    }

    // 自动续期
    private void renewTime(String lockName, String uuid, Long expire){
        String script = "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) " +
                "then return redis.call('expire', KEYS[1], ARGV[2]) end;";
        this.thread = new Thread(() -> {
            while(true){
                try {
                    Thread.sleep(expire *  1000 * 2 / 3); // 没过三分之二就续期一下
                    this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid, expire.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        this.thread.start();
    }
}
