package org.lpd.devicemanagermqtt.support.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET =
            "demo-secret-key-demo-secret-key-demo"; // ≥ 32字节

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    // 生成 token（你已有）
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 60 * 60 * 1000)
                )
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 token（核心）
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 在 JwtUtil 类中添加
    public Date getExpirationDate(Claims claims) {
        return claims.getExpiration();
    }
}
