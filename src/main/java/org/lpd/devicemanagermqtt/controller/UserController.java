package org.lpd.devicemanagermqtt.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.lpd.devicemanagermqtt.payload.request.LoginRequest;
import org.lpd.devicemanagermqtt.payload.request.SignupRequest;
import org.lpd.devicemanagermqtt.payload.response.LoginResponse;
import org.lpd.devicemanagermqtt.service.UserService;
import org.lpd.devicemanagermqtt.support.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Resource
    private JwtUtil jwtUtil;


    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        // 1. 基础非空校验
        if (signUpRequest.getUsername() == null || signUpRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body("错误：用户名或密码不能为空！");
        }

        try {
            // 2. 调用 Service 层执行 MD5 加盐注册
            userService.register(signUpRequest);

            return ResponseEntity.ok("用户注册成功！");
        } catch (RuntimeException e) {
            // 捕获“用户名已存在”等业务异常
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("服务器错误，请稍后再试");
        }
    }

    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody LoginRequest request) {

        // 1. 简单校验
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        // 1.1 调用 Service 层执行登录 在数据库层面进行校验

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
