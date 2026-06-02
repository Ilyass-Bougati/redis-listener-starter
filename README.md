> Deprecated. This starter provided annotation-driven Redis Pub/Sub before it existed in the framework. As of spring-data-redis 4.1, `@RedisListener` is supported natively (see [#3321](https://github.com/spring-projects/spring-data-redis/pull/3321)). Use the framework's built-in support instead.

# Redis Listener 
![redis](https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)

This package removes the overhead of configuring redis sub/pub listeners by adding a `@RedisListener` annotation.

### Adding it
First you can add this package to your application by adding it to your `pom.xml` if you're using Maven
```xml
<dependency>
    <groupId>com.sefault.redis</groupId>
    <artifactId>redis-listener-spring-boot-starter</artifactId>
    <version>1.4.0</version>
</dependency>
```
if you're using Gradle you can add it to your `build.gradle`
```gradle
implementation("com.sefault.redis:redis-listener-spring-boot-starter:1.4.0")
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
