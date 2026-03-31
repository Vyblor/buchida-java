package io.buchida;

import java.util.*;

public class Emails {

    private final Buchida client;

    Emails(Buchida client) {
        this.client = client;
    }

    public Map<String, Object> send(Map<String, Object> params) {
        return client.request("POST", "/emails", params);
    }

    public Map<String, Object> get(String id) {
        return client.request("GET", "/emails/" + id);
    }

    public Map<String, Object> list() {
        return list(Map.of());
    }

    public Map<String, Object> list(Map<String, String> params) {
        StringBuilder qs = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!qs.isEmpty()) qs.append("&");
            qs.append(entry.getKey()).append("=").append(entry.getValue());
        }
        String path = qs.isEmpty() ? "/emails" : "/emails?" + qs;
        return client.request("GET", path);
    }

    public void cancel(String id) {
        client.request("POST", "/emails/" + id + "/cancel");
    }

    public List<Map<String, Object>> sendBatch(List<Map<String, Object>> emails) {
        String json = JsonHelper.toJsonArray(emails);
        return client.requestList("POST", "/emails/batch", json);
    }
}
