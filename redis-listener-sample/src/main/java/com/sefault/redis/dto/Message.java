package com.sefault.redis.dto;

import java.util.UUID;

public record Message(UUID id, String message) {
}
