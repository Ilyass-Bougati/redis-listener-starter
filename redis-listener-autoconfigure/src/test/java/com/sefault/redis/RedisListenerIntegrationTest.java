package com.sefault.redis;

import com.sefault.redis.annotation.RedisListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RedisListenerIntegrationTest.TestConfig.class)
@Import(RedisListenerAutoConfiguration.class)
class RedisListenerIntegrationTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TestReceiver receiver;

    // This forces the test to look at your local Docker Redis
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> 6379);
    }

    @Test
    void testEndToEndMessageDelivery() throws InterruptedException {
        String messagePayload = "Hello from Integration Test " + System.currentTimeMillis();
        System.out.println("TEST: Sending message -> " + messagePayload);

        redisTemplate.convertAndSend("integration-test", messagePayload);

        boolean messageReceived = receiver.getLatch().await(5, TimeUnit.SECONDS);

        assertThat(messageReceived).isTrue().withFailMessage("Timed out waiting for Redis message!");
        assertThat(receiver.getLastMessage()).isEqualTo(messagePayload);

        System.out.println("TEST: Verified message reception!");
    }

    @SpringBootApplication
    static class TestConfig {

        @Bean
        public TestReceiver testReceiver() {
            return new TestReceiver();
        }
        
    }

    static class TestReceiver {
        private final CountDownLatch latch = new CountDownLatch(1);
        private String lastMessage;

        @RedisListener(channel = "integration-test")
        public void receive(String message) {
            System.out.println("LISTENER: Caught message -> " + message);
            this.lastMessage = message;
            latch.countDown();
        }

        public CountDownLatch getLatch() { return latch; }
        public String getLastMessage() { return lastMessage; }
    }
}
