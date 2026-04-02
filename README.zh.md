<div align="center">
  <img src="assets/logo-black.svg" alt="buchida" width="280" />
  <p><strong>支持CJK的开发者优先邮件API</strong></p>

  [English](README.md) | [한국어](README.ko.md) | [日本語](README.ja.md) | [中文](README.zh.md)

  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

---

[buchida](https://buchida.com)邮件API的官方Java SDK。

## 安装

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

## 快速开始

```java
import io.buchida.Buchida;
import java.util.Map;

var buchida = new Buchida("bc_live_xxxxxxxxxxxxxxxxxxxxx");

var result = buchida.emails().send(Map.of(
    "from", "hello@yourdomain.com",
    "to", "user@example.com",
    "subject", "欢迎使用buchida！",
    "html", "<h1>你好！</h1><p>欢迎加入。</p>"
));

System.out.println("邮件发送成功: " + result.get("id"));
```

## 特性

- Java 17+
- 零依赖（`java.net.http.HttpClient`）
- 类型化异常层次结构

## 文档

- [快速开始](https://buchida.com/zh/docs/quickstart)
- [API参考](https://buchida.com/zh/docs/sending-email)
- [GitHub](https://github.com/Vyblor/buchida-java)

## 许可证

MIT
