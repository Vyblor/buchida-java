package io.buchida;

import java.util.Map;

public class Metrics {

    private final Buchida client;

    Metrics(Buchida client) {
        this.client = client;
    }

    public Map<String, Object> get(String from, String to) {
        return get(from, to, null);
    }

    public Map<String, Object> get(String from, String to, String granularity) {
        StringBuilder qs = new StringBuilder("from=").append(from).append("&to=").append(to);
        if (granularity != null) {
            qs.append("&granularity=").append(granularity);
        }
        return client.request("GET", "/metrics?" + qs);
    }
}
