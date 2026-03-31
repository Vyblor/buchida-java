package io.buchida;

import java.util.List;
import java.util.Map;

public class Templates {

    private final Buchida client;

    Templates(Buchida client) {
        this.client = client;
    }

    public List<Map<String, Object>> list() {
        return client.requestList("GET", "/templates");
    }

    public Map<String, Object> get(String id) {
        return client.request("GET", "/templates/" + id);
    }
}
