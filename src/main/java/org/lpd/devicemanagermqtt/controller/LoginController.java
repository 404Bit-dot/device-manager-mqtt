package org.lpd.devicemanagermqtt.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.lpd.devicemanagermqtt.payload.request.LoginRequest;
import org.lpd.devicemanagermqtt.payload.response.LoginResponse;
import org.lpd.devicemanagermqtt.support.security.JwtUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class LoginController {

    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        // 1. 简单校验
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        // 2. 生成 token
        String token = jwtUtil.generateToken(request.getUsername());

        // 3. 构造返回对象
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(request.getUsername());

        // demo 阶段写死
        response.setUserId(1L);
        response.setRoles(List.of("USER"));

        return response;
    }



    @PostMapping("/test")
    public String test(HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");


        System.out.println("userId = " + userId);
        System.out.println("username = " + username);


        return "ok";
    }


}
