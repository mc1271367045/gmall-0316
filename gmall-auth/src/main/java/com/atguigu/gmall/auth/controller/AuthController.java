package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/07/14:15
 * @Description:
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    // 跳转登陆页面，获取returnUrl
    @GetMapping("toLogin")
    public String toLogin(@RequestParam(value = "returnUrl", defaultValue = "http://gmall.com")String returnUrl, Model model){
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    // 登录方法
    @PostMapping("login")
    public String login(@RequestParam(value = "returnUrl", defaultValue = "http://gmall.com")String returnUrl,
                        @RequestParam(value = "loginName")String loginName,
                        @RequestParam(value = "password")String password,
                        HttpServletRequest request, HttpServletResponse response) throws Exception {
        this.authService.accredit(loginName, password, request, response);
        return "redirect:" + returnUrl;
    }
}
