package com.sefault.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a listener for Redis messages.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisListener {
    /**
     * The Redis channel/topic to subscribe to.
     */
    String topic();

    /**
     * This enables wildcards and topic patterns
     */
    boolean usePattern() default false;
}
