package com.sefault.redis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageSender implements CommandLineRunner {
    private final StringRedisTemplate redisTemplate;

    public MessageSender(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Sending test message in 2 seconds...");
        Thread.sleep(2000);

        redisTemplate.convertAndSend("test-channel", "Hello from the custom starter!");
    }
}
