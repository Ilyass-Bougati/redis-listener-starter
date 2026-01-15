package com.sefault.redis.sender;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Order(2)
@Component
public class JsonMessageSender implements CommandLineRunner {
    private final StringRedisTemplate redisTemplate;

    public JsonMessageSender(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Sending test message in 2 seconds...");
        Thread.sleep(2000);

        String json = "{\"id\":\"" + UUID.randomUUID().toString() + "\",\"message\":\"test\"}";
        redisTemplate.convertAndSend("json-test-channel", json);
    }
}
