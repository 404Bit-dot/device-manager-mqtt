package org.lpd.devicemanagermqtt.service;


import org.lpd.devicemanagermqtt.models.test.DeviceInfo;
import org.lpd.devicemanagermqtt.support.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DeviceService {

    @Autowired
    private RedisUtils redisUtils;

    // 存储数据到 Redis
    public void saveDeviceToCache(DeviceInfo deviceInfo) {
        // 构建 Key，例如 device:info:1001
        String key = "device:info:" + deviceInfo.getId();
        // 存入 Redis，并设置 1 小时过期
        redisUtils.set(key, deviceInfo, 1, TimeUnit.HOURS);
    }

    // 从 Redis 获取数据
    public DeviceInfo getDeviceFromCache(String id) {
        String key = "device:info:" + id;
        return (DeviceInfo) redisUtils.get(key);
    }
}