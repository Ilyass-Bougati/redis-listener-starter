package com.sefault.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JacksonDateTest {

    static class TestMessageDto {
        public String message;
        public LocalDateTime createdAt;

        public TestMessageDto() {}
    }

    @Test
    void shouldParseJava8Dates() throws Exception {
        String jsonPayload = "{\"message\":\"Hello Spring\", \"createdAt\":\"2026-01-17T10:15:30\"}";

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        TestMessageDto result = mapper.readValue(jsonPayload, TestMessageDto.class);

        assertNotNull(result.createdAt, "Date should not be null");
        assertEquals(2026, result.createdAt.getYear());
        assertEquals(10, result.createdAt.getHour());
    }
}
