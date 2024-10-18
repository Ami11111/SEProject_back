package com.library.backend.utils;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "mySecretKey"; // 用于签名的密钥
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // Token 有效期：1小时

    // 生成 JWT Token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 设置主体，通常是用户信息
                .setIssuedAt(new Date()) // Token 的发行时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 使用 HS256 加密算法
                .compact();
    }

    // 验证 Token 是否有效
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 从 Token 中获取用户名
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
