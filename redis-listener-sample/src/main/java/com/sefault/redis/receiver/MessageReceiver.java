package com.sefault.redis.receiver;

import com.sefault.redis.annotation.RedisListener;
import com.sefault.redis.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageReceiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RedisListener(topic = "test-channel")
    public void handleMessage(String message) {
        logger.info("Successfully handled normal message: {}", message);
    }

    @RedisListener(topic = "test-json-channel")
    public void handleJsonMessage(Message message) {
        logger.info("Successfully handled json message: {}", message.id().toString());
    }

    @RedisListener(topic = "test-*", usePattern = true)
    public void handleBoth(String message) {
        logger.info("Successfully handled message using pattern: {}", message);
    }
}
