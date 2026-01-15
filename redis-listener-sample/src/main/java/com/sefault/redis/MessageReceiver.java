package com.sefault.redis;

import com.sefault.redis.annotation.RedisListener;
import org.springframework.stereotype.Service;

@Service
public class MessageReceiver {
    @RedisListener(channel = "test-channel")
    public void handleMessage(String message) {
        System.out.println("SUCCESS! Received message: " + message);
    }
}
