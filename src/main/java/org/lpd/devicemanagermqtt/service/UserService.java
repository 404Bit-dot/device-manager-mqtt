package org.lpd.devicemanagermqtt.service;

import org.lpd.devicemanagermqtt.mapper.UserMapper;
import org.lpd.devicemanagermqtt.models.entity.User;
import org.lpd.devicemanagermqtt.payload.request.SignupRequest;
import org.lpd.devicemanagermqtt.support.security.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    // 盐值：注册和登录必须一致
    private static final String SALT = "LPD_MQTT_KEY_2024";

    /**
     * 基础注册逻辑：跑通流程专用
     */
    public void register(SignupRequest signUpRequest) {
        // 1. 简单的业务查重（不带锁，先跑通）
        User existingUser = userMapper.findByUsername(signUpRequest.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 构造实体类
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setStatus(0); // 0-正常

        // 3. MD5 加密
        String hashedPassword = Md5Utils.encode(signUpRequest.getPassword() + SALT);
        user.setPasswordHash(hashedPassword);

        // 4. 设置时间（如果数据库没有自动生成，代码里必须给）
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        // 5. 写入数据库
        userMapper.insert(user);
    }

    /**
     * 基础登录逻辑
     */
    public User login(String username, String rawPassword) {
        // 1. 查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 比对 MD5
        String inputHashed = Md5Utils.encode(rawPassword + SALT);
        if (!user.getPasswordHash().equalsIgnoreCase(inputHashed)) {
            throw new RuntimeException("用户名或密码错误");
        }

        return user;
    }
}