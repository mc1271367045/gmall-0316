package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/07/14:15
 * @Description:
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;

    public void accredit(String loginName, String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 1.远程调用接口，查询用户名和密码是否正确
        ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();

        // 2.判断用户是否为空
        if (userEntity == null) {
            throw new UserException("用户名或者密码有误！！！");
        }

        // 3.生成jwt载荷信息
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userEntity.getId());
        map.put("username", userEntity.getUsername());
        // 4.防止盗用jwt，加入了用户的ip地址
        String ip = IpUtil.getIpAddressAtService(request);
        map.put("ip", ip);
        // 5.生成jwt类型的token
        String jwt = JwtUtils.generateToken(map, this.jwtProperties.getPrivateKey(), jwtProperties.getExpire());

        // 6.把jwt类型的token放入cookie中
        CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(), jwt, jwtProperties.getExpire() * 60);
        // 登陆成功之后显示用户昵称
        CookieUtils.setCookie(request, response, this.jwtProperties.getUnick(), userEntity.getNickname(), jwtProperties.getExpire() * 60);
    }
}

