package org.lpd.devicemanagermqtt.support.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MqttService {

    @Resource
    private MessageChannel mqttOutputChannel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handleIncomingMessage(String topic, String payload) {
        log.info("收到 MQTT 消息 → topic={}, payload={}", topic, payload);

        if (payload == null || payload.isBlank()) {
            log.warn("空 MQTT 消息，忽略");
            return;
        }

        if (isJson(payload)) {
            handleJsonMessage(topic, payload);
        } else {
            handlePlainMessage(topic, payload);
        }
    }

    private void handleJsonMessage(String topic, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);

            String device = root.path("device").asText(null);
            String status = root.path("status").asText(null);

            log.info("JSON解析 → device={}, status={}", device, status);

            // 👉 后续：存库 / Redis / 规则引擎 / Kafka
        } catch (Exception e) {
            log.error("JSON解析失败: {}", payload, e);
        }
    }

    private void handlePlainMessage(String topic, String payload) {
        log.info("普通文本消息 → {}", payload);
    }

    private boolean isJson(String payload) {
        payload = payload.trim();
        return (payload.startsWith("{") && payload.endsWith("}")) ||
                (payload.startsWith("[") && payload.endsWith("]"));
    }

    // 发送消息
    public void sendMessage(String topic, String payload) {
        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("mqtt_topic", topic)
                .build();
        mqttOutputChannel.send(message);
        log.info("发送 MQTT → topic={}, payload={}", topic, payload);
    }
}
