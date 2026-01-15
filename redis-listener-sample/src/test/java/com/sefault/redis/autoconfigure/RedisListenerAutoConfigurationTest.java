package com.sefault.redis.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sefault.redis.RedisListenerAutoConfiguration;
import com.sefault.redis.config.RedisListenerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisListenerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisListenerAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void containerIsCreated_whenConnectionFactoryExists() {
        contextRunner
                .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisMessageListenerContainer.class);
                    assertThat(context).hasSingleBean(RedisListenerConfig.class);
                });
    }

    @Test
    void containerFails_whenConnectionFactoryIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasRootCauseInstanceOf(org.springframework.beans.factory.NoSuchBeanDefinitionException.class);
        });
    }
}
