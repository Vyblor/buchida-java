package io.buchida;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class Buchida {

    private static final String DEFAULT_BASE_URL = "https://api.buchida.com";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final String VERSION = "0.1.0";

    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;

    private final Emails emails;
    private final Domains domains;
    private final ApiKeys apiKeys;
    private final Webhooks webhooks;
    private final Templates templates;
    private final Metrics metrics;

    public Buchida(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL, DEFAULT_TIMEOUT, null);
    }

    public Buchida(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, DEFAULT_TIMEOUT, null);
    }

    public Buchida(String apiKey, String baseUrl, Duration timeout, HttpClient httpClient) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        this.apiKey = apiKey;
        this.baseUrl = baseUrl != null ? baseUrl.replaceAll("/$", "") : DEFAULT_BASE_URL;
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .connectTimeout(timeout != null ? timeout : DEFAULT_TIMEOUT)
                .build();

        this.emails = new Emails(this);
        this.domains = new Domains(this);
        this.apiKeys = new ApiKeys(this);
        this.webhooks = new Webhooks(this);
        this.templates = new Templates(this);
        this.metrics = new Metrics(this);
    }

    public Emails emails() { return emails; }
    public Domains domains() { return domains; }
    public ApiKeys apiKeys() { return apiKeys; }
    public Webhooks webhooks() { return webhooks; }
    public Templates templates() { return templates; }
    public Metrics metrics() { return metrics; }

    Map<String, Object> request(String method, String path) {
        return request(method, path, (String) null);
    }

    Map<String, Object> request(String method, String path, Map<String, Object> body) {
        return request(method, path, body != null ? JsonHelper.toJson(body) : null);
    }

    Map<String, Object> request(String method, String path, String jsonBody) {
        try {
            String url = baseUrl + path;
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "buchida-java/" + VERSION);

            if (jsonBody != null) {
                builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else if ("POST".equals(method)) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else if ("DELETE".equals(method)) {
                builder.DELETE();
            } else {
                builder.GET();
            }

            HttpResponse<String> response = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String responseBody = response.body();

            if (status >= 400) {
                throwError(status, responseBody);
            }

            if (status == 204 || responseBody == null || responseBody.isEmpty()) {
                return Map.of();
            }

            return JsonHelper.parseObject(responseBody);
        } catch (BuchidaException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            throw new BuchidaException("Request failed: " + e.getMessage(), 0);
        }
    }

    List<Map<String, Object>> requestList(String method, String path) {
        return requestList(method, path, null);
    }

    List<Map<String, Object>> requestList(String method, String path, String jsonBody) {
        try {
            String url = baseUrl + path;
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "buchida-java/" + VERSION);

            if (jsonBody != null) {
                builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                builder.GET();
            }

            HttpResponse<String> response = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String responseBody = response.body();

            if (status >= 400) {
                throwError(status, responseBody);
            }

            if (status == 204 || responseBody == null || responseBody.isEmpty()) {
                return List.of();
            }

            return JsonHelper.parseArray(responseBody);
        } catch (BuchidaException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            throw new BuchidaException("Request failed: " + e.getMessage(), 0);
        }
    }

    private void throwError(int status, String body) {
        Map<String, Object> errorBody = Map.of();
        try {
            errorBody = JsonHelper.parseObject(body);
        } catch (Exception ignored) {}

        String message = errorBody.containsKey("message")
                ? errorBody.get("message").toString()
                : "Unknown error";

        switch (status) {
            case 401 -> throw new BuchidaException.AuthenticationException(message);
            case 404 -> throw new BuchidaException.NotFoundException(message);
            case 422 -> throw new BuchidaException.ValidationException(message);
            case 429 -> throw new BuchidaException.RateLimitException(message);
            default -> throw new BuchidaException(message, status);
        }
    }
}
