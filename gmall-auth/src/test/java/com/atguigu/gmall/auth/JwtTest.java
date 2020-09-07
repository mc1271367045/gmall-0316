package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建目录
    private static final String pubKeyPath = "E:\\javaDianshang\\rsa\\rsa.pub";
    private static final String priKeyPath = "E:\\javaDianshang\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "23fghfgh@#$;/4");
    }

    // 在生成公私钥（第一个Test）之前不要打开该注释
    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "4136");
        map.put("username", "gork");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjQxMzYiLCJ1c2VybmFtZSI6ImdvcmsiLCJleHAiOjE1OTk0NjAxMzl9.XTsURCYjUQPhdK8TPO3AwoU4F30d4YafqX5lU-uGHQPJ4F13Ap2eguTxuHzlgSSzagR8TOlYLkMHFzNX1WJqZaeOk5cFDUsngmexvairO3X4CAD4wkgX1ek99BQyxu_UPRCz2_T2Kb8-EQ0ljc-p7fYhj4IzBCpRplO4Scf1y3Ig5J1rcNQQ07aaBVmYonWarDsNWMGytIJvmxioTBEPOJaWhHPLUxG0OC-QG_h9aAcVDD7UgUKJsYUwltCaL6Y94XWslvHH_ld04piP85oRBt_TSCsvy78ve0OgdK-W8Naji_sMKqdccNBvWh1cVZHVpKTjzaobodsxgu4fgfVHog";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}