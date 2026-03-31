# buchida-java

Official Java SDK for the [buchida](https://buchida.com) email API.

## Installation

### Gradle

```groovy
implementation 'io.buchida:buchida-java:0.1.0'
```

### Maven

```xml
<dependency>
    <groupId>io.buchida</groupId>
    <artifactId>buchida-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick Start

```java
import io.buchida.Buchida;
import java.util.Map;

var buchida = new Buchida("bc_live_xxxxxxxxxxxxxxxxxxxxx");

var result = buchida.emails().send(Map.of(
    "from", "hello@yourdomain.com",
    "to", "user@example.com",
    "subject", "Welcome to buchida!",
    "html", "<h1>Hello!</h1><p>Welcome aboard.</p>"
));

System.out.println("Email sent: " + result.get("id"));
```

## Features

- Java 17+
- Zero dependencies (`java.net.http.HttpClient`)
- Typed exception hierarchy

## Error Handling

```java
try {
    buchida.emails().send(params);
} catch (BuchidaException.AuthenticationException e) {
    // 401 - invalid API key
} catch (BuchidaException.RateLimitException e) {
    // 429 - too many requests
} catch (BuchidaException e) {
    System.err.println(e.getStatusCode() + ": " + e.getMessage());
}
```

## License

MIT
