package com.sefault.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RedisListenerAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RedisListenerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        log.info("Configuring default RedisMessageListenerContainer for @RedisListener support.");
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    public RedisListenerBeanPostProcessor redisListenerBeanPostProcessor(RedisMessageListenerContainer container) {
        return new RedisListenerBeanPostProcessor(container);
    }
}
