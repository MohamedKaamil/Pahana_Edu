package com.pahana.bookshop.util;

import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

public class JsonUtil {
    
    public static String toJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value.toString());
            } else if (value instanceof LocalDateTime) {
                json.append("\"").append(((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\"");
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    public static String createErrorJson(String message) {
        return "{\"error\": \"" + escapeJson(message) + "\"}"; 
    }
    
    public static String createSuccessJson(String message) {
        return "{\"message\": \"" + escapeJson(message) + "\"}"; 
    }
    
    public static Map<String, String> parseSimpleJson(BufferedReader reader) throws Exception {
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        
        return parseJsonString(jsonBuilder.toString());
    }
    
    public static Map<String, String> parseJsonString(String jsonStr) {
        Map<String, String> result = new HashMap<>();
        
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return result;
        }
        
        // Remove outer braces
        jsonStr = jsonStr.trim();
        if (jsonStr.startsWith("{")) {
            jsonStr = jsonStr.substring(1);
        }
        if (jsonStr.endsWith("}")) {
            jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
        }
        
        // Simple parsing - split by comma and parse key:value pairs
        String[] pairs = jsonStr.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                result.put(key, value);
            }
        }
        
        return result;
    }
    
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    public static String userToJson(com.pahana.bookshop.model.User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("createdAt", user.getCreatedAt());
        return toJson(data);
    }
    
}