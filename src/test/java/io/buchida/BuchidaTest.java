package io.buchida;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

class BuchidaTest {

    private static HttpServer server;
    private static int port;
    private static String responseBody = "{}";
    private static int responseStatus = 200;
    private static String lastRequestBody = "";

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();

        server.createContext("/", exchange -> {
            // Verify auth header
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes());

            byte[] response = responseBody.getBytes();
            exchange.sendResponseHeaders(responseStatus, responseStatus == 204 ? -1 : response.length);
            if (responseStatus != 204) {
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
            }
            exchange.close();
        });

        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @BeforeEach
    void resetMock() {
        responseBody = "{}";
        responseStatus = 200;
    }

    private Buchida client() {
        return new Buchida("bc_test_xxx", "http://localhost:" + port);
    }

    @Test
    void testRequiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new Buchida(""));
        assertThrows(IllegalArgumentException.class, () -> new Buchida(null));
    }

    @Test
    void testInitializesServices() {
        Buchida b = client();
        assertNotNull(b.emails());
        assertNotNull(b.domains());
        assertNotNull(b.apiKeys());
        assertNotNull(b.webhooks());
        assertNotNull(b.templates());
        assertNotNull(b.metrics());
    }

    // ── Emails ──────────────────────────────────────────────────────────

    @Test
    void testEmailsSend() {
        responseBody = "{\"data\":{\"id\":\"email_123\"}}";
        var result = client().emails().send(Map.of(
                "from", "hi@buchida.com",
                "to", "user@example.com",
                "subject", "Hello",
                "html", "<p>Hi</p>"
        ));
        // Map-returning methods keep the raw envelope: payload lives under "data".
        assertEquals("email_123", ((Map<?, ?>) result.get("data")).get("id"));
    }

    @Test
    void testEmailsSendUsesSnakeCaseWireKeys() {
        responseBody = "{\"data\":{\"id\":\"email_123\"}}";
        client().emails().send(Map.of(
                "from", "hi@buchida.com",
                "from_name", "Vyblor",
                "to", "user@example.com",
                "subject", "Hello",
                "reply_to", "support@buchida.com"
        ));
        assertTrue(lastRequestBody.contains("\"from_name\":\"Vyblor\""), lastRequestBody);
        assertTrue(lastRequestBody.contains("\"reply_to\":\"support@buchida.com\""), lastRequestBody);
        assertFalse(lastRequestBody.contains("replyTo"), lastRequestBody);
    }

    @Test
    void testEmailsGet() {
        responseBody = "{\"data\":{\"id\":\"email_123\",\"from\":\"hi@buchida.com\",\"to\":[\"user@example.com\"],\"subject\":\"Hello\",\"status\":\"delivered\",\"created_at\":\"2026-03-31T00:00:00Z\"}}";
        var result = client().emails().get("email_123");
        var email = (Map<?, ?>) result.get("data");
        assertEquals("delivered", email.get("status"));
        assertEquals("2026-03-31T00:00:00Z", email.get("created_at"));
    }

    @Test
    void testEmailsList() {
        responseBody = "{\"data\":[{\"id\":\"e1\"},{\"id\":\"e2\"}],\"cursor\":\"e2\",\"hasMore\":true}";
        var result = client().emails().list(Map.of("limit", "10", "status", "delivered"));
        var items = (List<?>) result.get("data");
        assertEquals(2, items.size());
        assertEquals("e2", result.get("cursor"));
        assertEquals(true, result.get("hasMore"));
    }

    @Test
    void testEmailsCancel() {
        responseStatus = 204;
        assertDoesNotThrow(() -> client().emails().cancel("email_123"));
    }

    @Test
    void testEmailsSendBatch() {
        responseBody = "{\"data\":[{\"data\":{\"id\":\"email_1\",\"status\":\"queued\"}},{\"error\":{\"code\":\"invalid_email_address\",\"message\":\"bad to\"}}]}";
        var result = client().emails().sendBatch(List.of(
                Map.of("from", "hi@buchida.com", "to", "a@example.com", "subject", "A"),
                Map.of("from", "hi@buchida.com", "to", "b@example.com", "subject", "B")
        ));
        assertEquals(2, result.size());
        assertEquals("email_1", ((Map<?, ?>) result.get(0).get("data")).get("id"));
        assertNull(result.get(0).get("error"));
        assertEquals("invalid_email_address", ((Map<?, ?>) result.get(1).get("error")).get("code"));
    }

    // ── Domains ─────────────────────────────────────────────────────────

    @Test
    void testDomainsCreate() {
        responseBody = "{\"data\":{\"id\":\"dom_1\",\"name\":\"example.com\",\"status\":\"pending\",\"records\":[],\"created_at\":\"2026-03-31T00:00:00Z\"}}";
        var result = client().domains().create("example.com");
        assertEquals("example.com", ((Map<?, ?>) result.get("data")).get("name"));
    }

    @Test
    void testDomainsList() {
        responseBody = "{\"data\":[{\"id\":\"dom_1\",\"name\":\"a.com\",\"created_at\":\"2026-03-31T00:00:00Z\"},{\"id\":\"dom_2\",\"name\":\"b.com\",\"created_at\":\"2026-04-01T00:00:00Z\"}]}";
        var result = client().domains().list();
        assertEquals(2, result.size());
        assertEquals("b.com", result.get(1).get("name"));
        assertEquals("2026-04-01T00:00:00Z", result.get(1).get("created_at"));
    }

    @Test
    void testDomainsVerify() {
        responseBody = "{\"data\":{\"id\":\"dom_1\",\"name\":\"example.com\",\"status\":\"verified\",\"records\":[],\"created_at\":\"2026-03-31T00:00:00Z\"}}";
        var result = client().domains().verify("dom_1");
        assertEquals("verified", ((Map<?, ?>) result.get("data")).get("status"));
    }

    // ── API Keys ────────────────────────────────────────────────────────

    @Test
    void testApiKeysCreate() {
        responseBody = "{\"data\":{\"id\":\"key_1\",\"name\":\"test\",\"key\":\"bc_live_newkey\",\"permission\":\"full_access\",\"created_at\":\"2026-03-31T00:00:00Z\"}}";
        var result = client().apiKeys().create("test", "full_access");
        assertEquals("bc_live_newkey", ((Map<?, ?>) result.get("data")).get("key"));
    }

    @Test
    void testApiKeysDelete() {
        responseStatus = 204;
        assertDoesNotThrow(() -> client().apiKeys().delete("key_1"));
    }

    // ── Webhooks ────────────────────────────────────────────────────────

    @Test
    void testWebhooksCreate() {
        responseBody = "{\"data\":{\"id\":\"wh_1\",\"url\":\"https://example.com/wh\",\"events\":[\"email.delivered\"],\"created_at\":\"2026-03-31T00:00:00Z\"}}";
        var result = client().webhooks().create("https://example.com/wh", List.of("email.delivered"));
        assertEquals("wh_1", ((Map<?, ?>) result.get("data")).get("id"));
    }

    // ── Templates ───────────────────────────────────────────────────────

    @Test
    void testTemplatesList() {
        responseBody = "{\"data\":[{\"id\":\"tpl_1\",\"name\":\"Welcome\"}]}";
        var result = client().templates().list();
        assertEquals(1, result.size());
        assertEquals("Welcome", result.get(0).get("name"));

        responseBody = "{\"data\":[]}";
        assertTrue(client().templates().list().isEmpty());
    }

    @Test
    void testTemplatesGet() {
        responseBody = "{\"data\":{\"id\":\"tpl_1\",\"name\":\"Welcome\",\"created_at\":\"2026-03-31T00:00:00Z\"}}";
        var result = client().templates().get("tpl_1");
        assertEquals("Welcome", ((Map<?, ?>) result.get("data")).get("name"));
    }

    // ── Metrics ─────────────────────────────────────────────────────────

    @Test
    void testMetricsGet() {
        responseBody = "{\"data\":{\"sent\":100,\"delivered\":95,\"opened\":50,\"clicked\":10,\"bounced\":3,\"complained\":1,\"timeseries\":[]}}";
        var result = client().metrics().get("2026-03-01", "2026-03-31", "day");
        assertEquals(100L, ((Map<?, ?>) result.get("data")).get("sent"));
    }

    // ── Error Handling ──────────────────────────────────────────────────

    @Test
    void test401ThrowsAuthenticationException() {
        responseStatus = 401;
        responseBody = "{\"message\":\"Invalid API key\"}";
        assertThrows(BuchidaException.AuthenticationException.class,
                () -> client().emails().list());
    }

    @Test
    void test429ThrowsRateLimitException() {
        responseStatus = 429;
        responseBody = "{\"message\":\"Rate limit exceeded\"}";
        assertThrows(BuchidaException.RateLimitException.class,
                () -> client().emails().list());
    }

    @Test
    void test500ThrowsBuchidaException() {
        responseStatus = 500;
        responseBody = "{\"message\":\"Internal server error\"}";
        assertThrows(BuchidaException.class,
                () -> client().emails().list());
    }

    @Test
    void testNestedErrorMessageSurfaced() {
        responseStatus = 422;
        responseBody = "{\"error\":{\"code\":\"validation_error\",\"message\":\"from_name is invalid\"}}";
        var ex = assertThrows(BuchidaException.ValidationException.class,
                () -> client().emails().send(Map.of("from", "a@b.com")));
        assertEquals("from_name is invalid", ex.getMessage());
    }

    @Test
    void testNestedErrorCodeOnGenericError() {
        responseStatus = 500;
        responseBody = "{\"error\":{\"code\":\"internal_error\",\"message\":\"boom\"}}";
        var ex = assertThrows(BuchidaException.class, () -> client().emails().list());
        assertEquals("boom", ex.getMessage());
        assertEquals("internal_error", ex.getErrorCode());
    }

    // The API is mounted under /v1; without it every call returned 404.
    @Test
    void testDefaultBaseUrlIncludesV1() throws Exception {
        var field = Buchida.class.getDeclaredField("baseUrl");
        field.setAccessible(true);
        assertEquals("https://api.buchida.com/v1", field.get(new Buchida("bc_test_xxx")));
    }
}
