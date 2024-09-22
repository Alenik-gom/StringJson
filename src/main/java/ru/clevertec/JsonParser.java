package ru.clevertec;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

public class JsonParser {

    public static Map<String, Object> parseJson(String json) {
        // Удаляем первую и последнюю фигурные скобки
        if (json.charAt(0) == '{' && json.charAt(json.length() - 1) == '}') {
            json = json.trim().substring(1, json.length() - 1);
        }
        //новая мапа
        Map<String, Object> resultMap = new HashMap<>();

        StringBuilder buffer = new StringBuilder(json);

        while (!buffer.isEmpty()) {
            String key = extractNextKey(buffer);
            Object value = extractNextValue(buffer);
            resultMap.put(key, value);

            if (!buffer.isEmpty() && buffer.charAt(0) == ',') {
                buffer.deleteCharAt(0);  // Удаление ведущей запятой
            }
        }

        return resultMap;
    }

    private static String extractNextKey(StringBuilder buffer) {
        int startKey = buffer.indexOf("\"") + 1;
        int endKey = buffer.indexOf("\"", startKey);
        String key = buffer.substring(startKey, endKey);
        buffer.delete(0, endKey + 2); // Удаляем ключ и следующие за ним ":"
        return key.trim();
    }

    private static Object extractNextValue(StringBuilder buffer) {
        if (buffer.charAt(0) == '[') {
            return extractList(buffer);
        } else if (buffer.charAt(0) == '{') {
            String nestedJson = extractNestedObject(buffer);
            return parseJson(nestedJson);
        } else {
            int nextComma = buffer.indexOf(",");
            int nextBrace = buffer.indexOf("}");
            int endIndex = nextComma != -1 ? nextComma : nextBrace;

            if (endIndex == -1) {
                endIndex = buffer.length();
            }

            String value = buffer.substring(0, endIndex).trim();

            // Удалить из buffer
            buffer.delete(0, endIndex);

            // Вырезать кавычки для строк
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            // Проверить на число
//            try {
 //               return Double.parseDouble(value);
 //           } catch (NumberFormatException e) {
                return value;
 //          }
        }
    }

    private static List<Object> extractList(StringBuilder buffer) {
        List<Object> list = new ArrayList<>();
        int endListIndex = findClosingIndex(buffer, '[', ']');

        // Извлекает внутреннюю часть списка
        String listContent = buffer.substring(1, endListIndex).trim();
        buffer.delete(0, endListIndex + 2);

        StringBuilder itemListBuffer = new StringBuilder(listContent);
        while (!itemListBuffer.isEmpty()) {
            Object item = extractNextValue(itemListBuffer);
            list.add(item);

            if (!itemListBuffer.isEmpty() && itemListBuffer.charAt(0) == ',') {
                itemListBuffer.deleteCharAt(0);
            }
        }
        return list;
    }

    private static String extractNestedObject(StringBuilder buffer) {
        int endObjectIndex = findClosingIndex(buffer, '{', '}');
        String nestedObject = buffer.substring(1, endObjectIndex).trim();
        buffer.delete(0, endObjectIndex + 2);
        return nestedObject;
    }

    private static int findClosingIndex(StringBuilder buffer, char open, char close) {
        int depth = 1;
        int index = 1;
        // сравниваем глубину скобок
        while (depth > 0 && index < buffer.length()) {
            if (buffer.charAt(index) == open) {
                depth++;
            } else if (buffer.charAt(index) == close) {
                depth--;
            }
            index++;
        }

        return index - 1;
    }
    public static String buildObjectJsonString(Object object) throws Exception {
        if (object == null) {
            return "null";
        }
        Class<?> objClass = object.getClass();
        Map<String, Object> jsonElements = new HashMap<>();

        for (Field field : objClass.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(object);
            jsonElements.put(field.getName(), valueToString(value));
        }

        StringBuilder json = new StringBuilder("{");
        boolean isFirst = true;

        for (Map.Entry<String, Object> entry : jsonElements.entrySet()) {
            if (!isFirst) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            isFirst = false;
        }

        json.append("}");
        return json.toString();
    }
    private static String escapeString(String value) {
        return value.replace("\"", "\\\"");
    }

    private static String addQuotes(String value) {
        return "\"" + value + "\"";
    }

    private static String valueToString(Object value) throws Exception {

        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof UUID || value instanceof LocalDate || value instanceof OffsetDateTime) {
            return addQuotes(value.toString());
        } else if (value instanceof List) {
            return createfromList(value);
        } else if (Map.class.isAssignableFrom(value.getClass())) {
            return createfromMap(value);
        } else if (Object.class.isAssignableFrom(value.getClass())) {
            return buildObjectJsonString(value);
        }
        return null;
    }

    private static String createfromMap(Object mapObject) throws Exception {
        if (!(mapObject instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Expected a map object");
        }
        Map<?, ?> map = (Map<?, ?>) mapObject;
        StringBuilder jsonMap = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                jsonMap.append(",");
            }
            jsonMap.append("\"" + escapeString(entry.getKey().toString()) + "\":");
            jsonMap.append(valueToString(entry.getValue()));
            first = false;
        }
        jsonMap.append("}");
        return jsonMap.toString();
    }

    private static String createfromList(Object object) throws Exception {
        if (!(object instanceof List<?>)) {
            throw new IllegalArgumentException("Provided value is not a List: " + object);
        }
        List<Object> list = (List<Object>) object;
        StringBuilder jsonList = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                jsonList.append(",");
            }
            jsonList.append(valueToString(item));
            first = false;
        }
        jsonList.append("]");
        return jsonList.toString();
    }
}
