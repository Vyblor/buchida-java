package io.buchida;

import java.util.List;
import java.util.Map;

public class ApiKeys {

    private final Buchida client;

    ApiKeys(Buchida client) {
        this.client = client;
    }

    public Map<String, Object> create(String name, String permission) {
        return client.request("POST", "/api-keys", Map.of(
                "name", name,
                "permission", permission
        ));
    }

    public List<Map<String, Object>> list() {
        return client.requestList("GET", "/api-keys");
    }

    public void delete(String id) {
        client.request("DELETE", "/api-keys/" + id);
    }
}
