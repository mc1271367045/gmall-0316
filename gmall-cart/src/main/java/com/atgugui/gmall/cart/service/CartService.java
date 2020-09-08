package com.atgugui.gmall.cart.service;

import com.atgugui.gmall.cart.feign.GmallPmsClient;
import com.atgugui.gmall.cart.feign.GmallSmsClient;
import com.atgugui.gmall.cart.feign.GmallWmsClient;
import com.atgugui.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/08/14:12
 * @Description:
 */
@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    // 加入购物车
    public void saveCart(Cart cart) {


    }

    // 加入购物车显示页面
    public Cart queryCartBySkuId(Long skuId) {

        return null;
    }
}
