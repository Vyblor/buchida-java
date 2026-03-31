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

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();

        server.createContext("/", exchange -> {
            // Verify auth header
            String auth = exchange.getRequestHeaders().getFirst("Authorization");

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
        responseBody = "{\"id\":\"email_123\"}";
        var result = client().emails().send(Map.of(
                "from", "hi@buchida.com",
                "to", "user@example.com",
                "subject", "Hello",
                "html", "<p>Hi</p>"
        ));
        assertEquals("email_123", result.get("id"));
    }

    @Test
    void testEmailsGet() {
        responseBody = "{\"id\":\"email_123\",\"from\":\"hi@buchida.com\",\"to\":[\"user@example.com\"],\"subject\":\"Hello\",\"status\":\"delivered\",\"createdAt\":\"2026-03-31T00:00:00Z\"}";
        var result = client().emails().get("email_123");
        assertEquals("delivered", result.get("status"));
    }

    @Test
    void testEmailsList() {
        responseBody = "{\"data\":[],\"cursor\":null}";
        var result = client().emails().list(Map.of("limit", "10", "status", "delivered"));
        assertNotNull(result);
    }

    @Test
    void testEmailsCancel() {
        responseStatus = 204;
        assertDoesNotThrow(() -> client().emails().cancel("email_123"));
    }

    @Test
    void testEmailsSendBatch() {
        responseBody = "[{\"id\":\"email_1\"},{\"id\":\"email_2\"}]";
        var result = client().emails().sendBatch(List.of(
                Map.of("from", "hi@buchida.com", "to", "a@example.com", "subject", "A"),
                Map.of("from", "hi@buchida.com", "to", "b@example.com", "subject", "B")
        ));
        assertEquals(2, result.size());
    }

    // ── Domains ─────────────────────────────────────────────────────────

    @Test
    void testDomainsCreate() {
        responseBody = "{\"id\":\"dom_1\",\"name\":\"example.com\",\"status\":\"pending\",\"records\":[],\"createdAt\":\"2026-03-31T00:00:00Z\"}";
        var result = client().domains().create("example.com");
        assertEquals("example.com", result.get("name"));
    }

    @Test
    void testDomainsList() {
        responseBody = "[]";
        var result = client().domains().list();
        assertNotNull(result);
    }

    @Test
    void testDomainsVerify() {
        responseBody = "{\"id\":\"dom_1\",\"name\":\"example.com\",\"status\":\"verified\",\"records\":[],\"createdAt\":\"2026-03-31T00:00:00Z\"}";
        var result = client().domains().verify("dom_1");
        assertEquals("verified", result.get("status"));
    }

    // ── API Keys ────────────────────────────────────────────────────────

    @Test
    void testApiKeysCreate() {
        responseBody = "{\"id\":\"key_1\",\"name\":\"test\",\"key\":\"bc_live_newkey\",\"permission\":\"full_access\",\"createdAt\":\"2026-03-31T00:00:00Z\"}";
        var result = client().apiKeys().create("test", "full_access");
        assertEquals("bc_live_newkey", result.get("key"));
    }

    @Test
    void testApiKeysDelete() {
        responseStatus = 204;
        assertDoesNotThrow(() -> client().apiKeys().delete("key_1"));
    }

    // ── Webhooks ────────────────────────────────────────────────────────

    @Test
    void testWebhooksCreate() {
        responseBody = "{\"id\":\"wh_1\",\"url\":\"https://example.com/wh\",\"events\":[\"email.delivered\"],\"createdAt\":\"2026-03-31T00:00:00Z\"}";
        var result = client().webhooks().create("https://example.com/wh", List.of("email.delivered"));
        assertEquals("wh_1", result.get("id"));
    }

    // ── Templates ───────────────────────────────────────────────────────

    @Test
    void testTemplatesList() {
        responseBody = "[]";
        var result = client().templates().list();
        assertNotNull(result);
    }

    @Test
    void testTemplatesGet() {
        responseBody = "{\"id\":\"tpl_1\",\"name\":\"Welcome\",\"createdAt\":\"2026-03-31T00:00:00Z\"}";
        var result = client().templates().get("tpl_1");
        assertEquals("Welcome", result.get("name"));
    }

    // ── Metrics ─────────────────────────────────────────────────────────

    @Test
    void testMetricsGet() {
        responseBody = "{\"sent\":100,\"delivered\":95,\"opened\":50,\"clicked\":10,\"bounced\":3,\"complained\":1,\"timeseries\":[]}";
        var result = client().metrics().get("2026-03-01", "2026-03-31", "day");
        assertEquals(100L, result.get("sent"));
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
}
