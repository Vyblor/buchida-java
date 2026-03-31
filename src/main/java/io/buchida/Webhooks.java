package io.buchida;

import java.util.List;
import java.util.Map;

public class Webhooks {

    private final Buchida client;

    Webhooks(Buchida client) {
        this.client = client;
    }

    public Map<String, Object> create(String url, List<String> events) {
        return client.request("POST", "/webhooks", Map.of(
                "url", url,
                "events", events
        ));
    }

    public List<Map<String, Object>> list() {
        return client.requestList("GET", "/webhooks");
    }

    public void delete(String id) {
        client.request("DELETE", "/webhooks/" + id);
    }
}
