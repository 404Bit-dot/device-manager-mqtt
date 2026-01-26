package org.lpd.devicemanagermqtt.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.lpd.devicemanagermqtt.models.entity.User;
import org.lpd.devicemanagermqtt.payload.request.LoginRequest;
import org.lpd.devicemanagermqtt.payload.request.SignupRequest;
import org.lpd.devicemanagermqtt.payload.response.LoginResponse;
import org.lpd.devicemanagermqtt.service.UserService;
import org.lpd.devicemanagermqtt.support.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        // 1. 基础非空校验
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("错误：用户名或密码不能为空！");
        }

        try {
            // 2. 调用 Service 层执行真实的数据库校验
            // 这里会进行：1. 用户是否存在校验；2. MD5 加盐密码比对
            User user = userService.login(request.getUsername(), request.getPassword());

            // 3. 校验通过，生成真实的 JWT Token
            String token = jwtUtil.generateToken(user.getUsername());

            // 4. 构造返回对象，填充数据库中的真实数据
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(user.getUsername());
            response.setUserId(user.getId()); // 使用数据库自增的真实 ID

            // 暂时手动分配角色，后续可从数据库 roles 表读取
            response.setRoles(List.of("USER"));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // 捕获 Service 抛出的“用户名或密码错误”、“用户不存在”等业务异常
            // 返回 401 Unauthorized 状态码
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            // 捕获系统级异常
            return ResponseEntity.internalServerError().body("登录服务暂时不可用，请稍后再试");
        }
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
