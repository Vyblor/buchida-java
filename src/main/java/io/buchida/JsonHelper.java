package io.buchida;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal JSON helper using no external dependencies.
 * Handles simple object/array serialization and deserialization.
 */
final class JsonHelper {

    private JsonHelper() {}

    static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    static String toJsonArray(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Map<String, Object> item : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append(toJson(item));
        }
        sb.append("]");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return "\"" + escape(s) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map) return toJson((Map<String, Object>) value);
        if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                first = false;
                sb.append(valueToJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (value instanceof String[] arr) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escape(arr[i])).append("\"");
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(value.toString()) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    static Map<String, Object> parseObject(String json) {
        json = json.trim();
        if (json.startsWith("{")) {
            return parseObjectInner(json);
        }
        return Map.of();
    }

    static List<Map<String, Object>> parseArray(String json) {
        json = json.trim();
        List<Map<String, Object>> result = new ArrayList<>();
        if (!json.startsWith("[")) return result;

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return result;

        int depth = 0;
        int start = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            else if (c == ',' && depth == 0) {
                result.add(parseObjectInner(json.substring(start, i).trim()));
                start = i + 1;
            }
        }
        String last = json.substring(start).trim();
        if (!last.isEmpty()) {
            result.add(parseObjectInner(last));
        }
        return result;
    }

    private static Map<String, Object> parseObjectInner(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return map;
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return map;

        int i = 0;
        while (i < json.length()) {
            // Skip whitespace
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= json.length()) break;

            // Parse key
            if (json.charAt(i) != '"') break;
            int keyStart = ++i;
            while (i < json.length() && json.charAt(i) != '"') {
                if (json.charAt(i) == '\\') i++;
                i++;
            }
            String key = json.substring(keyStart, i);
            i++; // skip closing quote

            // Skip colon
            while (i < json.length() && (json.charAt(i) == ':' || Character.isWhitespace(json.charAt(i)))) i++;

            // Parse value
            Object value;
            if (i < json.length() && json.charAt(i) == '"') {
                int valStart = ++i;
                StringBuilder sb = new StringBuilder();
                while (i < json.length() && json.charAt(i) != '"') {
                    if (json.charAt(i) == '\\') {
                        i++;
                        if (i < json.length()) {
                            switch (json.charAt(i)) {
                                case 'n' -> sb.append('\n');
                                case 't' -> sb.append('\t');
                                case '"' -> sb.append('"');
                                case '\\' -> sb.append('\\');
                                default -> sb.append(json.charAt(i));
                            }
                        }
                    } else {
                        sb.append(json.charAt(i));
                    }
                    i++;
                }
                value = sb.toString();
                i++; // skip closing quote
            } else if (i < json.length() && json.charAt(i) == '{') {
                int depth = 0;
                int objStart = i;
                do {
                    if (json.charAt(i) == '{') depth++;
                    else if (json.charAt(i) == '}') depth--;
                    i++;
                } while (depth > 0 && i < json.length());
                value = parseObjectInner(json.substring(objStart, i));
            } else if (i < json.length() && json.charAt(i) == '[') {
                int depth = 0;
                int arrStart = i;
                do {
                    if (json.charAt(i) == '[') depth++;
                    else if (json.charAt(i) == ']') depth--;
                    i++;
                } while (depth > 0 && i < json.length());
                String arrStr = json.substring(arrStart, i);
                // Try to parse as array of objects or strings
                value = parseGenericArray(arrStr);
            } else if (i < json.length() && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '-')) {
                int numStart = i;
                while (i < json.length() && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '.' || json.charAt(i) == '-')) i++;
                String numStr = json.substring(numStart, i);
                if (numStr.contains(".")) {
                    value = Double.parseDouble(numStr);
                } else {
                    value = Long.parseLong(numStr);
                }
            } else if (i + 4 <= json.length() && json.substring(i, i + 4).equals("true")) {
                value = true;
                i += 4;
            } else if (i + 5 <= json.length() && json.substring(i, i + 5).equals("false")) {
                value = false;
                i += 5;
            } else if (i + 4 <= json.length() && json.substring(i, i + 4).equals("null")) {
                value = null;
                i += 4;
            } else {
                break;
            }

            map.put(key, value);

            // Skip comma
            while (i < json.length() && (json.charAt(i) == ',' || Character.isWhitespace(json.charAt(i)))) i++;
        }

        return map;
    }

    private static Object parseGenericArray(String arrStr) {
        arrStr = arrStr.trim();
        if (!arrStr.startsWith("[") || !arrStr.endsWith("]")) return List.of();
        String inner = arrStr.substring(1, arrStr.length() - 1).trim();
        if (inner.isEmpty()) return List.of();

        if (inner.startsWith("{")) {
            return parseArray(arrStr);
        }

        // Parse as string array
        List<String> strings = new ArrayList<>();
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(inner);
        while (m.find()) {
            strings.add(m.group(1));
        }
        if (!strings.isEmpty()) return strings;

        return List.of();
    }
}
