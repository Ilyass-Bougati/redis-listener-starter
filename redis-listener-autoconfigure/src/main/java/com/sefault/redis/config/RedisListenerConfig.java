package com.sefault.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sefault.redis.ReflectionMessageListener;
import com.sefault.redis.annotation.RedisJsonListener;
import com.sefault.redis.annotation.RedisListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.lang.reflect.Method;

@Configuration
public class RedisListenerConfig implements SmartInitializingSingleton {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisMessageListenerContainer container;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();

            for (Method method : beanClass.getMethods()) {

                if (method.isAnnotationPresent(RedisListener.class)) {
                    RedisListener ann = method.getAnnotation(RedisListener.class);
                    register(bean, method, ann.topic(), null, ann.usePattern());
                }

                else if (method.isAnnotationPresent(RedisJsonListener.class)) {
                    RedisJsonListener ann = method.getAnnotation(RedisJsonListener.class);
                    register(bean, method, ann.topic(), ann.type(), ann.usePattern());
                }
            }
        }
    }

    private void register(Object bean, Method method, String channel, Class<?> type, boolean usePattern) {
        if (method.getParameterCount() != 1) {
            throw new IllegalStateException("Method " + method.getName() + " must have exactly 1 parameter.");
        }

        MessageListener listener = new ReflectionMessageListener(bean, method, type, objectMapper);
        if (usePattern) {
            container.addMessageListener(listener, new PatternTopic(channel));
        } else {
            container.addMessageListener(listener, new ChannelTopic(channel));
        }
        logger.debug("Registered Redis listener: " + method.getName() + " on channel " + channel);
    }
}
