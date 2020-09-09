package com.atgugui.gmall.cart.controller;

import com.atgugui.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/08/14:11
 * @Description:
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    // 加入购物车(href链接 get请求)
    @GetMapping
    public String addCart(Cart cart){
        if (cart == null || cart.getSkuId() == null){
            throw new RuntimeException("你没有选中的任何商品！！");
        }

        this.cartService.saveCart(cart);
        return "redirect:http://cart.gmall.com/addCart?skuId=" + cart.getSkuId();
    }

    // 加入购物车显示页面
    @GetMapping("addCart")
    public String queryCartBySkuId(@RequestParam("skuId")Long skuId, Model model){
        Cart cart = this.cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    // 查询购物车
    @GetMapping("cart.html")
    public String queryCarts(Model model){
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo<Object> updateNum(@RequestBody Cart cart){

        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo<Object> deleteCart(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }


}
