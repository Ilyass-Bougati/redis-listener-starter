# Redis Listener
This package removes the overhead of configuring redis sub/pub listeners by adding a `@RedisListener` annotation.

### Adding it
First you can add this package to your application by adding it to your `pom.xml` if you're using Maven
```xml
<dependency>
    <groupId>com.sefault.redis</groupId>
    <artifactId>redis-listener-spring-boot-starter</artifactId>
    <version>1.2.0</version>
</dependency>
```
if you're using Gradle you can add it to your `build.gradle`
```gradle
implementation("com.sefault.redis:redis-listener-spring-boot-starter:1.2.0")
```

### Using it
`@RedisListener` annotates a method that's meant to handle receiving a message on a certain channel. For example
```java
@Service
public class MessageReceiver {
    @RedisListener(channel = "test-channel")
    public void handleMessage(String message) {
        System.out.println("SUCCESS! Received message: " + message);
    }

    @RedisListener(channel = "test-channel")
    public void handleMessage(MessageDto message) {
        System.out.println("SUCCESS! Received message: " + message.getId());
    }
}
```

You can also use patterns and wildcards, and you can also define a error topic to send errors in
```java
@Service
public class MessageReceiver {
    @RedisListener(channel = "test-*", errorChannel="test-error-channel")
    public void handleMessage(String message) {
        System.out.println("SUCCESS! Received message: " + message);
    }
}
```
you can also define a default error channel in your `application.properties`
```properties
redis.starter.default-error-channel=test-error-channel
```