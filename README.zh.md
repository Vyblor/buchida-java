<div align="center">
  <img src="assets/logo-black.svg" alt="buchida" width="280" />
  <p><strong>buchida Java SDK — 为 AI 代理打造的邮件 API</strong></p>

  [English](README.md) | [한국어](README.ko.md) | [日本語](README.ja.md) | [**中文**](README.zh.md)

  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

---

io.buchida 是为 AI 代理打造的邮件 API 的官方 Java SDK。buchida 提供 CLI、MCP 服务器和 5 种语言的 SDK (Node、Python、Go、Ruby、Java),所有这些都共享相同的 REST API 表面。`@buchida/email` 模板原生渲染韩语、日语和中文。

## 安装

```xml
<dependency>
    <groupId>io.buchida</groupId>
    <artifactId>buchida-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 发送您的第一封邮件

```java
import io.buchida.Buchida;

Buchida buchida = new Buchida(System.getenv("BUCHIDA_API_KEY"));

buchida.emails().send(new SendEmailRequest.Builder()
    .from("hello@yourapp.com")
    .to("user@example.com")
    .subject("你好")
    .html("<h1>欢迎</h1>")
    .build());
```

## 文档

完整文档: **[buchida.com/docs](https://buchida.com/docs)**

- API 参考: https://buchida.com/docs/api-reference
- 快速入门指南: https://buchida.com/docs/quickstart
- CJK 邮件模板: https://buchida.com/docs/templates
- MCP 服务器设置: https://buchida.com/docs/mcp
- CLI 参考: https://buchida.com/docs/cli

## 链接

- **网站:** [buchida.com](https://buchida.com)
- **文档:** [buchida.com/docs](https://buchida.com/docs)
- **定价:** [buchida.com/pricing](https://buchida.com/pricing)
- **GitHub:** https://github.com/Vyblor/buchida-java

## 许可证

MIT
