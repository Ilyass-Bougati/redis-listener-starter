# Redis Listener
This package removes the overhead of configuring redis sub/pub listeners by adding a `@RedisListener` annotation.

### Adding it
First you can add this package to your application by adding it to your `pom.xml` if you're using Maven
```xml
<dependency>
    <groupId>com.sefault.redis</groupId>
    <artifactId>redis-listener-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```
if you're using Gradle you can add it to your `build.gradle`
```gradle
implementation("com.sefault.redis:redis-listener-spring-boot-starter:1.0.0")
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
}
```