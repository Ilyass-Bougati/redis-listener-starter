package com.sefault.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReflectionMessageListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private Message redisMessage;

    private TestService testService;

    @BeforeEach
    void setUp() {
        testService = spy(new TestService());
    }

    @Test
    void shouldInvokeJsonListenerWithParsedObject() throws Exception {
        String jsonPayload = "{\"username\":\"akira\"}";
        UserEvent expectedEvent = new UserEvent("akira");
        Method method = TestService.class.getMethod("handleUserEvent", UserEvent.class);

        when(redisMessage.getBody()).thenReturn(jsonPayload.getBytes());
        when(objectMapper.readValue(any(byte[].class), eq(UserEvent.class))).thenReturn(expectedEvent);

        ReflectionMessageListener listener = new ReflectionMessageListener(
                testService,
                method,
                UserEvent.class,
                objectMapper,
                "error-channel",
                stringRedisTemplate
        );

        listener.onMessage(redisMessage, null);
        verify(testService, times(1)).handleUserEvent(expectedEvent);
    }

    @Test
    void shouldInvokeStringListenerWithRawString() throws Exception {
        String rawPayload = "System failure imminent";
        Method method = TestService.class.getMethod("handleRawLog", String.class);

        when(redisMessage.getBody()).thenReturn(rawPayload.getBytes());

        ReflectionMessageListener listener = new ReflectionMessageListener(
                testService,
                method,
                String.class,
                objectMapper,
                "error-channel",
                stringRedisTemplate
        );

        listener.onMessage(redisMessage, null);
        verify(testService, times(1)).handleRawLog(rawPayload);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenParsingFails() throws Exception {
        Method method = TestService.class.getMethod("handleUserEvent", UserEvent.class);

        when(redisMessage.getBody()).thenReturn("{bad_json}".getBytes());
        when(objectMapper.readValue(any(byte[].class), eq(UserEvent.class)))
                .thenThrow(new IOException("Malformed JSON"));

        ReflectionMessageListener listener = new ReflectionMessageListener(
                testService,
                method,
                UserEvent.class,
                objectMapper,
                "error-channel",
                stringRedisTemplate
        );

        assertThrows(RuntimeException.class, () -> {
            listener.onMessage(redisMessage, null);
        });
    }

    static record UserEvent(String username) {}

    static class TestService {
        public void handleUserEvent(UserEvent event) {
            System.out.println("Handled event: " + event);
        }

        public void handleRawLog(String log) {
            System.out.println("Handled log: " + log);
        }
    }
}