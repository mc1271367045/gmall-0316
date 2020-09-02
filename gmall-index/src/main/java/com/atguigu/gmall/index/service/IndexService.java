package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.utils.DistributeLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/01/16:06
 * @Description:
 */
@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "index:cates:";

    @Autowired
    private DistributeLock distributeLock;

    // 查询一级菜单
    public List<CategoryEntity> queryLv1lCategories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesByPid(0l);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }

    // 查询二级三级菜单
    public List<CategoryEntity> queryCategoriesWithSubByPid(Long pid) {
        // 1 查询缓存
        String json = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(json != null){
            return JSON.parseArray(json, CategoryEntity.class);
        }

        // 2 远程调用，查询数据库，并放入缓存
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesWithSubByPid(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();

        // 为了解决缓存穿透，返回值为null依然要缓存
        // 为了解决缓存雪崩，过期时间设置随机值
        this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(categoryEntities),30 + new Random().nextInt(10), TimeUnit.DAYS);

        return categoryEntities;
    }

    // 子方法
    private void testLock3(String lockName, String uuid) {

        this.distributeLock.tryLock(lockName, uuid, 30L);

        System.out.println("这是一个子方法，也需要获取锁。。。。。。");

        this.distributeLock.unLock(lockName, uuid);
    }

    // 分布式锁demo2 调用封装的DistributeLock方法，保证可重入
    public void testLock2() {

        // 通过setnx获取锁  设置过期时间，防止死锁发生
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.distributeLock.tryLock("lock", uuid, 30L);

        if(lock){
            // 获取成功，执行业务
            String numString = this.stringRedisTemplate.opsForValue().get("num");
            if(StringUtils.isBlank(numString)){
                this.stringRedisTemplate.opsForValue().set("num","0");
                return;
            }
            int num = Integer.parseInt(numString);
            this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
        }

        this.testLock3("lock", uuid);

        // 释放锁
        this.distributeLock.unLock("lock",uuid);
    }

    // 分布式锁demo1
    public void testLock1() {

        // 通过setnx获取锁  设置过期时间，防止死锁发生
        String uuid = UUID.randomUUID().toString();
        Boolean lock = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);

        // 获取失败，重试
        if(!lock){
            try {TimeUnit.MILLISECONDS.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
            testLock1();
        }else {
            //this.stringRedisTemplate.expire("lock",3, TimeUnit.SECONDS); // 在此处无法保证再宕机情况下设置有效
            // 获取成功，执行业务
            String numString = this.stringRedisTemplate.opsForValue().get("num");
            if(StringUtils.isBlank(numString)){
                this.stringRedisTemplate.opsForValue().set("num","0");
                return;
            }
            int num = Integer.parseInt(numString);
            this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));

            // 释放锁
            // 为了防止误删，需要判断当前的锁是不是自己的锁
            // 为了保证原子性，这里使用LUA脚本
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
//            if(StringUtils.equals(uuid,this.stringRedisTemplate.opsForValue().get("lock"))){
//                this.stringRedisTemplate.delete("lock");
//            }
        }
    }
}























