package com.atgugui.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atgugui.gmall.cart.Interceptor.LoginInterceptor;
import com.atgugui.gmall.cart.feign.GmallPmsClient;
import com.atgugui.gmall.cart.feign.GmallSmsClient;
import com.atgugui.gmall.cart.feign.GmallWmsClient;
import com.atgugui.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/08/14:12
 * @Description:
 */
@Service
public class CartService {

//    @Autowired
//    private CartMapper cartMapper;

    @Autowired
    private CartAsyncService cartAsyncService;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:info:";

    private static final String PRICE_PREFIX = "cart:price:";

    // 加入购物车
    public void saveCart(Cart cart) {
        // 1、获取用户的登录信息（组装key）
        String userId = null;
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null){
            userId = userInfo.getUserId().toString();
        } else {
            userId = userInfo.getUserKey();
        }
        String key = KEY_PREFIX + userId;

        // 2、获取内层map的操作对象（该用户所有购物车的集合）{8: cart, 9:cart}
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 判断该用户购物车中是否已有该商品
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount(); // 获得你添加商品的数量
        if (hashOps.hasKey(skuId)) {
            // 有，更新数量
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));

            // 新增mysql数据库
            //this.cartMapper.updateCartByUserIdAndSkuId(userId, cart);
            this.cartAsyncService.updateCartByUserIdAndSkuId(userId, cart);
        } else {
            // 无，给该用户新增一条记录
            cart.setUserId(userId);
            cart.setCheck(true);

            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null){
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                cart.setDefaultImage(skuEntity.getDefaultImage());
            }

            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            // 查询销售属性信息
            ResponseVo<List<SkuAttrValueEntity>> saleAttrResponseVo = this.pmsClient.querySaleAttrValueBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            // 查询营销信息
            ResponseVo<List<ItemSaleVo>> listResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = listResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            // 新增mysql数据库
            this.cartAsyncService.addCart(userId,cart);
            // 并且新增价格的缓存，如果已经有人把该商品加入了购物车，该商品的价格缓存已存在。这时依然进行加缓存，相当于做了价格同步
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        // redis中，更新和新增都是put方法
        hashOps.put(skuId, JSON.toJSONString(cart));


    }

    // 加入购物车显示页面
    public Cart queryCartBySkuId(Long skuId) {
        String key = KEY_PREFIX;
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null){
            key += userInfo.getUserId();
        } else {
            key += userInfo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(skuId.toString())){
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        } else {
            throw new RuntimeException("该用户不存在对应商品的购物车记录！");
        }
    }

    // 查询购物车
    public List<Cart> queryCarts() {
        // 1.查询未登录的购物车
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        String unLoginKey = KEY_PREFIX + userKey;
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(unLoginKey);
        // 获取未登录的购物车集合
        List<Object> unLoginCartJsons = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(unLoginCartJsons)) {
            unLoginCarts = unLoginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                // 查询redis中的实时价格缓存设置给查询结果集
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        // 2.判断登录状态
        Long userId = userInfo.getUserId();
        if (userId == null) {
            // 3.未登录，返回未登录的购物车
            return unLoginCarts;
        }

        // 4.登录，合并未登录的购物车到登录状态的购物车
        String loginKey = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                BigDecimal count = cart.getCount(); // 未登录购物车的数量
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(cartJson, Cart.class); // 登录情况下的购物车
                    cart.setCount(cart.getCount().add(count));

                    // 更新mysql中数量
                    this.cartAsyncService.updateCartByUserIdAndSkuId(userId.toString(), cart);
                } else {
                    cart.setUserId(userId.toString());
                    this.cartAsyncService.addCart(userId.toString(), cart);  // 要覆盖mysql中的Userkey的数据（没有userkey的数据 转化未对应的userId的数据）
                }
                // 更新redis的数量
                loginHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });
        }

        // 5.删除未登录的购物车
        this.cartAsyncService.deleteCartsByUserId(userKey); // mysql（异步删除）
        this.redisTemplate.delete(unLoginKey); // redis

        // 6.查询登录状态的购物车
        List<Object> loginCartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJsons)) {
            return loginCartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }
        return null;
    }

    // 更新购物车
    public void updateNum(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = null;
        if (userInfo.getUserId() != null){
            userId = userInfo.getUserId().toString();
        } else {
            userId = userInfo.getUserKey();
        }
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        BigDecimal count = cart.getCount();
        if (hashOps.hasKey(cart.getSkuId().toString())){
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);

            // 异步更新mysql
            this.cartAsyncService.updateCartByUserIdAndSkuId(userId, cart);
            // 更新redis
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }
    }

    // 删除购物车
    public void deleteCart(Long skuId) {

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = null;
        if (userInfo.getUserId() != null){
            userId = userInfo.getUserId().toString();
        } else {
            userId = userInfo.getUserKey();
        }
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(skuId.toString())){
            // 删除mysql中的数据
            this.cartAsyncService.deleteCartByUserIdAndSkuId(userId, skuId);
            // 删除redis中的数据
            hashOps.delete(skuId.toString());
        }
    }

}
