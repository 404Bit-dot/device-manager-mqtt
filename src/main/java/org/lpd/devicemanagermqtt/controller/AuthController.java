package org.lpd.devicemanagermqtt.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.lpd.devicemanagermqtt.support.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private RedisUtils redisUtils;

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Claims claims = (Claims) request.getAttribute("claims");

        if (token != null && claims != null) {
            // 计算 Token 剩余寿命 (毫秒)
            long diff = claims.getExpiration().getTime() - System.currentTimeMillis();
            
            if (diff > 0) {
                // 将该 Token 存入 Redis，过期时间设置为 Token 的剩余有效时间
                redisUtils.set("jwt:blacklist:" + token, "logout", diff, TimeUnit.MILLISECONDS);
            }
        }
        return "Logout Success";
    }
}