<div align="center">
  <img src="assets/logo-black.svg" alt="buchida" width="280" />
  <p><strong>buchida Java SDK — Email API for AI agents</strong></p>

  [**English**](README.md) | [한국어](README.ko.md) | [日本語](README.ja.md) | [中文](README.zh.md)

  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

---

io.buchida is the official Java SDK for **buchida** — an email API built for AI agents. buchida ships a CLI, an MCP server, and SDKs in 5 languages (Node, Python, Go, Ruby, Java), all sharing the same REST API surface. `@buchida/email` templates render Korean, Japanese, and Chinese natively.

## Install

```xml
<dependency>
    <groupId>io.buchida</groupId>
    <artifactId>buchida-java</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Send your first email

```java
import io.buchida.Buchida;
import java.util.List;
import java.util.Map;

Buchida buchida = new Buchida(System.getenv("BUCHIDA_API_KEY"));

Map<String, Object> res = buchida.emails().send(Map.of(
    "from", "hello@yourapp.com",
    "from_name", "Your App",
    "to", "user@example.com",
    "subject", "Hello",
    "html", "<h1>Welcome</h1>"
));

// Responses are returned as maps in the API's envelope: the payload is under "data".
Map<?, ?> email = (Map<?, ?>) res.get("data");
System.out.println(email.get("id") + " " + email.get("status"));

// Lists: { "data": [...], "cursor": "...", "hasMore": true }
Map<String, Object> page = buchida.emails().list(Map.of("limit", "20"));
List<?> items = (List<?>) page.get("data");

// Batch: one entry per input, each { "data": {...} } or { "error": { "code", "message" } }
List<Map<String, Object>> results = buchida.emails().sendBatch(List.of(
    Map.of("from", "hello@yourapp.com", "to", "a@example.com", "subject", "A", "text", "hi")
));

// Other list endpoints return the unwrapped items
List<Map<String, Object>> domains = buchida.domains().list();
```

## Upgrading to 0.2

- Methods returning a `Map` (`emails().send/get/list`, `domains().create/get/verify`,
  `apiKeys().create`, `webhooks().create`, `templates().get`, `metrics().get`) are unchanged:
  they return the API envelope, so the payload is under `"data"` (`res.get("data")`).
  Response fields are snake_case (`created_at`, `from_name`, `reply_to`, ...).
- Methods returning a `List<Map<String, Object>>` (`emails().sendBatch`, `domains().list`,
  `apiKeys().list`, `webhooks().list`, `templates().list`) used to return an empty list
  (they expected a bare JSON array, but the API answers `{"data": [...]}`). They now return the items.
- New: `"from_name"` is supported in `send`; request bodies use snake_case (`reply_to`,
  `scheduled_at`); nested API errors (`{"error": {"code", "message"}}`) surface their message and code.

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
