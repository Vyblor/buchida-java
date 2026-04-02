<div align="center">
  <img src="assets/logo-black.svg" alt="buchida" width="280" />
  <p><strong>CJKサポートを備えた開発者向けメールAPI</strong></p>

  [English](README.md) | [한국어](README.ko.md) | [日本語](README.ja.md) | [中文](README.zh.md)

  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

---

[buchida](https://buchida.com)メールAPIの公式Java SDKです。

## インストール

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

## クイックスタート

```java
import io.buchida.Buchida;
import java.util.Map;

var buchida = new Buchida("bc_live_xxxxxxxxxxxxxxxxxxxxx");

var result = buchida.emails().send(Map.of(
    "from", "hello@yourdomain.com",
    "to", "user@example.com",
    "subject", "buchidaへようこそ！",
    "html", "<h1>こんにちは！</h1><p>ご登録ありがとうございます。</p>"
));

System.out.println("メール送信完了: " + result.get("id"));
```

## 特徴

- Java 17+
- 依存関係ゼロ（`java.net.http.HttpClient`）
- 型付き例外階層

## ドキュメント

- [クイックスタート](https://buchida.com/ja/docs/quickstart)
- [APIリファレンス](https://buchida.com/ja/docs/sending-email)
- [GitHub](https://github.com/Vyblor/buchida-java)

## ライセンス

MIT
