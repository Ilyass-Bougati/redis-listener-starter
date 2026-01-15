package com.sefault.redis;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.util.ReflectionUtils;

public class RedisListenerBeanPostProcessor implements BeanPostProcessor {
    private final RedisMessageListenerContainer container;
    private static final Logger log = LoggerFactory.getLogger(RedisListenerBeanPostProcessor.class);

    public RedisListenerBeanPostProcessor(RedisMessageListenerContainer container) {
        this.container = container;
    }

    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            RedisListener annotation = method.getAnnotation(RedisListener.class);

            if (annotation != null) {
                log.debug("Found @RedisListener on method '{}' in bean '{}'. Verifying signature...", method.getName(), beanName);
                if (method.getParameterCount() != 1) {
                    log.error("Failed to register @RedisListener on {}.{}. Method must have exactly one parameter.", beanName, method.getName());
                    throw new IllegalStateException(
                            "Method " + method.getName() + " annotated with @RedisListener must have exactly one parameter (the message payload)."
                    );
                }

                if (!method.getParameterTypes()[0].equals(String.class)) {
                    log.warn("Method {}.{} expects type {}, but default adapter sends String. Ensure your serialization logic handles this.",
                            beanName, method.getName(), method.getParameterTypes()[0].getSimpleName());
                }

                try {
                    MessageListenerAdapter adapter = new MessageListenerAdapter(bean, method.getName());
                    adapter.afterPropertiesSet();

                    String channel = annotation.channel();
                    container.addMessageListener(adapter, new ChannelTopic(channel));

                    log.info("Active: Subscribed bean '{}' method '{}' to Redis channel '{}'", beanName, method.getName(), channel);

                } catch (Exception e) {
                    log.error("CRITICAL: Failed to register Redis listener for {}.{}", beanName, method.getName(), e);
                    throw new RuntimeException("Could not register Redis listener", e);
                }
            }
        });

        return bean;
    }
}
