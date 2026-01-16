package com.sefault.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ReflectionMessageListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(ReflectionMessageListener.class);

    private final Object bean;
    private final Method method;
    private final Class<?> targetType;
    private final ObjectMapper objectMapper;
    private final String finalErrorChannel;
    private final StringRedisTemplate stringRedisTemplate;

    public ReflectionMessageListener(Object bean,
                                     Method method,
                                     Class<?> targetType,
                                     ObjectMapper objectMapper,
                                     String finalErrorChannel,
                                     StringRedisTemplate stringRedisTemplate) {
        this.bean = bean;
        this.method = method;
        this.targetType = targetType;
        this.objectMapper = objectMapper;
        this.finalErrorChannel = finalErrorChannel;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();

        try {
            Object argument;

            if (targetType.equals(String.class)) {
                argument = new String(body, StandardCharsets.UTF_8);
            } else {
                argument = objectMapper.readValue(body, targetType);
            }

            method.invoke(bean, argument);

        } catch (Exception e) {
            handleException(e, body, new String(message.getChannel()));
        }
    }

    private void handleException(Exception e, byte[] originalBody, String originalChannel) {
        if (finalErrorChannel == null || finalErrorChannel.isEmpty()) {
            log.error("Error processing Redis message on channel '{}'. Cause: {}", originalChannel, e.getMessage(), e);
            return;
        }

        try {
            Map<String, Object> errorPayload = new HashMap<>();
            errorPayload.put("failedChannel", originalChannel);
            errorPayload.put("originalMessage", new String(originalBody, StandardCharsets.UTF_8));
            errorPayload.put("exception", e.getClass().getSimpleName());
            errorPayload.put("errorMessage", e.getMessage());
            errorPayload.put("timestamp", System.currentTimeMillis());

            String jsonPayload = objectMapper.writeValueAsString(errorPayload);

            stringRedisTemplate.convertAndSend(finalErrorChannel, jsonPayload);

            log.info("Message processing failed. Sent to error channel: {}", finalErrorChannel);

        } catch (Exception publishEx) {
            log.error("CRITICAL: Failed to send error payload to channel '{}'", finalErrorChannel, publishEx);
        }
    }
}