package org.lpd.devicemanagermqtt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.lpd.devicemanagermqtt.models.test.DeviceInfo;
import org.lpd.devicemanagermqtt.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Redis测试接口")
@RestController
@RequestMapping("/api/redis")
public class RedisController {

    @Autowired
    private DeviceService deviceService;

    @Operation(summary = "将设备数据插入Redis")
    @PostMapping("/save")
    public String save(@RequestBody DeviceInfo deviceInfo) {
        deviceService.saveDeviceToCache(deviceInfo);
        return "数据已成功保存到 Redis";
    }

    @Operation(summary = "从Redis获取设备数据")
    @GetMapping("/get/{id}")
    public DeviceInfo get(@PathVariable String id) {
        return deviceService.getDeviceFromCache(id);
    }
}