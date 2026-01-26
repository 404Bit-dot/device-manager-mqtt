package org.lpd.devicemanagermqtt.support.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lpd.devicemanagermqtt.support.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Set<String> WHITE_LIST = Set.of(
            "/auth/login",
            "/auth/signup",
            "/doc.html",
            "/swagger-ui.html",
            "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 1. 白名单放行
        if (WHITE_LIST.contains(uri)
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/webjars")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 2. 取 token
        String token = request.getHeader("Authorization");

        if (token == null || token.isBlank()) {
            unauthorized(response, "Missing token");
            return;
        }

        // 3. 校验 token
        try {
            Claims claims = jwtUtil.parseToken(token);

            // 可选：把用户名放进 request，后面 Controller 可用
            request.setAttribute("username", claims.getSubject());

        } catch (JwtException e) {
            // 过期、签名错误、被篡改，都会进这里
            unauthorized(response, "Invalid or expired token");
            return;
        }


        try {
            // 1. 解析 Token (这里会校验签名和是否过期)
            Claims claims = jwtUtil.parseToken(token);

            // 2. 【新增】黑名单校验
            // 只要能从 Redis 查到这个 token，说明它在黑名单里
            String blacklistKey = "jwt:blacklist:" + token;
            if (redisUtils.get(blacklistKey) != null) {
                unauthorized(response, "Token has been invalidated (Logged out)");
                return;
            }

            request.setAttribute("username", claims.getSubject());
            // 将 claims 存入 request 方便后面注销时快速获取过期时间
            request.setAttribute("claims", claims);

        } catch (JwtException e) {
            unauthorized(response, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);

        // 4. 校验通过，放行
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String msg)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + msg + "\"}");
    }
}
