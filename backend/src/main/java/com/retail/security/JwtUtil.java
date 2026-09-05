package com.retail.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具：HS256 签名，密钥长度需 >= 32 字节
 */
public class JwtUtil {

    /**
     * 生成 JWT
     *
     * @param secretKey  签名密钥
     * @param ttlMillis  有效期（毫秒）
     * @param claims     自定义载荷
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT，失败抛出异常
     */
    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
