package org.lpd.devicemanagermqtt.support.mqtt;

import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import java.nio.charset.StandardCharsets;

@Data
@ConfigurationProperties(prefix = "lpd.mqtt")
@Configuration
public class MqttIntegrationConfig {

    private String broker;
    private String clientId;
    private String username;
    private String password;
    private Integer qos = 1;
    private Integer connectionTimeout = 10;
    private Integer keepAliveInterval = 20;
    private Boolean automaticReconnect = true;
    private Boolean cleanSession = true;
    private String[] topics = new String[]{"device/#"};

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{broker});
        options.setUserName(username);
        options.setPassword(password != null ? password.toCharArray() : null);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setAutomaticReconnect(automaticReconnect);
        options.setCleanSession(cleanSession);

        factory.setConnectionOptions(options);
        return factory;
    }

    // Inbound: 接收消息
    @Bean
    public MqttPahoMessageDrivenChannelAdapter inboundAdapter(MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "-inbound",
                        factory,
                        topics
                );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter()); // 保持通用型
        adapter.setQos(qos);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // ⭐ 核心：通吃型 messageHandler
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler messageHandler(MqttService mqttService) {
        return message -> {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            Object payloadObj = message.getPayload();

            String payload;
            try {
                if (payloadObj instanceof byte[]) {
                    payload = new String((byte[]) payloadObj, StandardCharsets.UTF_8);
                } else if (payloadObj instanceof String) {
                    payload = (String) payloadObj;
                } else {
                    payload = payloadObj.toString(); // Map / Object
                }

                mqttService.handleIncomingMessage(topic, payload);
            } catch (Exception e) {
                // 防止单条消息异常打崩 MQTT 链路
                System.err.println("MQTT 消息处理异常 topic=" + topic + ", payload=" + payloadObj);
                e.printStackTrace();
            }
        };
    }

    // Outbound: 发送消息
    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler outboundHandler(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientId + "-outbound", factory);
        handler.setAsync(true);
        handler.setDefaultQos(qos);
        handler.setDefaultRetained(false);
        return handler;
    }
}
