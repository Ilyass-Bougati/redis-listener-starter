package com.sefault.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReflectionMessageListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Message redisMessage;

    // A dummy service to act as the "User's Bean"
    private TestService testService;

    @BeforeEach
    void setUp() {
        testService = spy(new TestService());
    }

    @Test
    void shouldInvokeJsonListenerWithParsedObject() throws Exception {
        // 1. Arrange
        String jsonPayload = "{\"username\":\"akira\"}";
        UserEvent expectedEvent = new UserEvent("akira");
        Method method = TestService.class.getMethod("handleUserEvent", UserEvent.class);

        // Mock Redis returning bytes
        when(redisMessage.getBody()).thenReturn(jsonPayload.getBytes());
        // Mock Jackson parsing bytes into our Object
        when(objectMapper.readValue(any(byte[].class), eq(UserEvent.class))).thenReturn(expectedEvent);

        // Create the listener instance (targetType = UserEvent.class)
        ReflectionMessageListener listener = new ReflectionMessageListener(
                testService,
                method,
                UserEvent.class,
                objectMapper
        );

        // 2. Act
        listener.onMessage(redisMessage, null);

        // 3. Assert
        verify(testService, times(1)).handleUserEvent(expectedEvent);
    }

    @Test
    void shouldInvokeStringListenerWithRawString() throws Exception {
        // 1. Arrange
        String rawPayload = "System failure imminent";
        Method method = TestService.class.getMethod("handleRawLog", String.class);

        when(redisMessage.getBody()).thenReturn(rawPayload.getBytes());
        // Note: We don't mock ObjectMapper here because the listener shouldn't use it for String types

        // Create listener with targetType = null (Legacy Mode)
        ReflectionMessageListener listener = new ReflectionMessageListener(
                testService,
                method,
                null,
                objectMapper
        );

        // 2. Act
        listener.onMessage(redisMessage, null);

        // 3. Assert
        verify(testService, times(1)).handleRawLog(rawPayload);
        verifyNoInteractions(objectMapper); // Ensure parser wasn't touched
    }

    @Test
    void shouldThrowRuntimeExceptionWhenParsingFails() throws Exception {
        // 1. Arrange
        Method method = TestService.class.getMethod("handleUserEvent", UserEvent.class);

        when(redisMessage.getBody()).thenReturn("{bad_json}".getBytes());
        when(objectMapper.readValue(any(byte[].class), eq(UserEvent.class)))
                .thenThrow(new IOException("Malformed JSON"));

        ReflectionMessageListener listener = new ReflectionMessageListener(
                testService,
                method,
                UserEvent.class,
                objectMapper
        );

        // 2. Act & Assert
        assertThrows(RuntimeException.class, () -> {
            listener.onMessage(redisMessage, null);
        });
    }

    // --- Helpers ---

    // The Dummy POJO
    static record UserEvent(String username) {}

    // The Dummy Service Class
    static class TestService {
        public void handleUserEvent(UserEvent event) {
            System.out.println("Handled event: " + event);
        }

        public void handleRawLog(String log) {
            System.out.println("Handled log: " + log);
        }
    }
}