package org.lpd.devicemanagermqtt.models.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 用户基础信息实体类
 * 对应数据库表: users
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /**
     * 登录用户名
     */
    private String username;
    /**
     * 加密后的密码密文
     */
    private String passwordHash;
    /**
     * 绑定邮箱
     */
    private String email;
    /**
     * 绑定手机号
     */
    private String mobile;
    /**
     * 账号状态: 0-正常, 1-禁用, 2-未激活
     */
    private Integer status;
    /**
     * 注册时间
     */
    private LocalDateTime createdTime;
    /**
     * 最后更新时间
     */
    private LocalDateTime updatedTime;
}