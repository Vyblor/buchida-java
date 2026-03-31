package io.buchida;

import java.util.List;
import java.util.Map;

public class Domains {

    private final Buchida client;

    Domains(Buchida client) {
        this.client = client;
    }

    public Map<String, Object> create(String name) {
        return client.request("POST", "/domains", Map.of("name", name));
    }

    public List<Map<String, Object>> list() {
        return client.requestList("GET", "/domains");
    }

    public Map<String, Object> get(String id) {
        return client.request("GET", "/domains/" + id);
    }

    public Map<String, Object> verify(String id) {
        return client.request("POST", "/domains/" + id + "/verify");
    }
}
