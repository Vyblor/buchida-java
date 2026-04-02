<div align="center">
  <img src="assets/logo-black.svg" alt="buchida" width="280" />
  <p><strong>CJK 지원을 갖춘 개발자 중심 이메일 API</strong></p>

  [English](README.md) | [한국어](README.ko.md) | [日本語](README.ja.md) | [中文](README.zh.md)

  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

---

[buchida](https://buchida.com) 이메일 API의 공식 Java SDK입니다.

## 설치

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

## 빠른 시작

```java
import io.buchida.Buchida;
import java.util.Map;

var buchida = new Buchida("bc_live_xxxxxxxxxxxxxxxxxxxxx");

var result = buchida.emails().send(Map.of(
    "from", "hello@yourdomain.com",
    "to", "user@example.com",
    "subject", "buchida에 오신 것을 환영합니다!",
    "html", "<h1>안녕하세요!</h1><p>가입을 환영합니다.</p>"
));

System.out.println("이메일 발송 완료: " + result.get("id"));
```

## 주요 기능

- Java 17+
- 의존성 없음 (`java.net.http.HttpClient`)
- 타입 기반 예외 계층 구조

## 문서

- [빠른 시작 가이드](https://buchida.com/ko/docs/quickstart)
- [API 레퍼런스](https://buchida.com/ko/docs/sending-email)
- [GitHub](https://github.com/Vyblor/buchida-java)

## 라이선스

MIT
