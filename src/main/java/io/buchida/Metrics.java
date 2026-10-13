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
        // The API filters on from_date/to_date; plain from/to were silently ignored.
        StringBuilder qs = new StringBuilder("from_date=").append(from).append("&to_date=").append(to);
        if (granularity != null) {
            qs.append("&granularity=").append(granularity);
        }
        return client.request("GET", "/metrics?" + qs);
    }
}
