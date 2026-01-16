package com.sefault.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisJsonListener {
    /**
     * The Redis channel/topic to subscribe to.
     */
    String topic();

    /**
     * The class type to deserialize the JSON into.
     */
    Class<?> type();

    /**
     * This enables wildcards and topic patterns
     */
    boolean usePattern() default false;
}
