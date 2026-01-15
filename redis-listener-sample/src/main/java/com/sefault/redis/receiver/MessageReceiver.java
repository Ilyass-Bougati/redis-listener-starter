package com.sefault.redis.receiver;

import com.sefault.redis.annotation.RedisJsonListener;
import com.sefault.redis.annotation.RedisListener;
import com.sefault.redis.dto.Message;
import com.sefault.redis.sender.MessageSender;
import org.springframework.stereotype.Service;

@Service
public class MessageReceiver {
    @RedisListener(channel = "test-channel")
    public void handleMessage(String message) {
        System.out.println("SUCCESS! Received message: " + message);
    }

    @RedisJsonListener(channel = "json-test-channel", type = Message.class)
    public void handleJsonMessage(Message message) {
        System.out.println("SUCCESS! Received message: " + message.id().toString());
    }
}
