package com.sefault.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.lang.reflect.Method;

public class ReflectionMessageListener implements MessageListener {
    private final Object bean;
    private final Method method;
    private final Class<?> targetType;
    private final ObjectMapper objectMapper;

    public ReflectionMessageListener(Object bean, Method method, Class<?> targetType, ObjectMapper objectMapper) {
        this.bean = bean;
        this.method = method;
        this.targetType = targetType;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            byte[] body = message.getBody();
            Object argument;

            if (targetType == null) {
                argument = new String(body);
            } else {
                argument = objectMapper.readValue(body, targetType);
            }

            method.invoke(bean, argument);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke Redis listener on method " + method.getName(), e);
        }
    }
}
