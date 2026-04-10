# io.buchida

**buchida Java SDK — Email API for AI agents**

io.buchida is the official Java SDK for **buchida** — an email API built for AI agents. buchida ships a CLI, an MCP server, and SDKs in 5 languages (Node, Python, Go, Ruby, Java), all sharing the same REST API surface. `@buchida/email` templates render Korean, Japanese, and Chinese natively.

## Install

```xml
<dependency>
    <groupId>io.buchida</groupId>
    <artifactId>buchida-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Send your first email

```java
import io.buchida.Buchida;

Buchida buchida = new Buchida(System.getenv("BUCHIDA_API_KEY"));

buchida.emails().send(new SendEmailRequest.Builder()
    .from("hello@yourapp.com")
    .to("user@example.com")
    .subject("Hello")
    .html("<h1>Welcome</h1>")
    .build());
```

## Documentation

Full docs: **[buchida.com/docs](https://buchida.com/docs)**

- API reference: https://buchida.com/docs/api-reference
- Quickstart guide: https://buchida.com/docs/quickstart
- CJK email templates: https://buchida.com/docs/templates
- MCP server setup: https://buchida.com/docs/mcp
- CLI reference: https://buchida.com/docs/cli

## Links

- **Website:** [buchida.com](https://buchida.com)
- **Documentation:** [buchida.com/docs](https://buchida.com/docs)
- **Pricing:** [buchida.com/pricing](https://buchida.com/pricing)
- **GitHub:** https://github.com/Vyblor/buchida-java

## License

MIT
