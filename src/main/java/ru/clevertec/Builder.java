package ru.clevertec;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

public class Builder {
    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        Map<String, Object> stringObjectMap = JsonParser.parseJson(json);
        return (T) createInstance(stringObjectMap, clazz);
    }

    public static String toJson(Object object) throws Exception {
        return JsonParser.buildObjectJsonString(object);
    }

    private static Object createInstance(Object mapValues, Class<?> instanceClass) throws Exception {
        if (!(mapValues instanceof HashMap<?, ?>)) {
            throw new IllegalArgumentException("Provided value is not a Map: " + mapValues);
        }
        Map<?, ?> values = (Map<String, Object>) mapValues;
        Object instance = instanceClass.getDeclaredConstructor().newInstance();
        for (Field subField : instanceClass.getDeclaredFields()) {
            subField.setAccessible(true);
            String subFieldName = subField.getName();
            Object subFieldValue = values.get(subFieldName);
            if (subFieldValue != null) {
                Object castedSubFieldValue = castValueToFieldType(subFieldValue, subField);
                subField.set(instance, castedSubFieldValue);
            }
        }
        return instance;
    }

    private static Object castValueToFieldType(Object value, Field field) throws Exception {
        if (value.equals("null")) return null;
        Class<?> fieldType = field.getType();
        if (fieldType.equals(List.class) || Map.class.isAssignableFrom(fieldType)) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            Class<?> itemType = (Class<?>) genericType.getActualTypeArguments()[0];
            Class<?> valueType = (fieldType.equals(Map.class)) ? (Class<?>) genericType.getActualTypeArguments()[1] : null;
            //create map/list
            return getValueByType(value, fieldType, itemType, valueType);
        }
        return getValueByType(value, fieldType, null, null);
    }

    private static Object getValueByType(Object value, Class<?> fieldType, Class<?> itemType, Class<?> valueType) throws Exception {
        if (value.equals("null")) return null;
        if (fieldType.isPrimitive()) {
            if (fieldType.equals(int.class)) {
                return ((Number) value).intValue();
            } else if (fieldType.equals(double.class)) {
                return ((Number) value).doubleValue();
            } else if (fieldType.equals(boolean.class)) {
                return Boolean.parseBoolean(value.toString());
            } else if (fieldType.equals(long.class)) {
                return ((Number) value).longValue();
            }
        } else if (fieldType.equals(String.class)) {
            return value.toString();
        } else if (fieldType.equals(UUID.class)) {
            return UUID.fromString(value.toString());
        } else if (fieldType.equals(Double.class)) {
            return Double.valueOf(value.toString());
        } else if (fieldType.equals(BigDecimal.class)) {
            return BigDecimal.valueOf(Double.parseDouble(value.toString()));
        } else if (fieldType.equals(LocalDate.class)) {
            return LocalDate.parse(value.toString());
        } else if (fieldType.equals(OffsetDateTime.class)) {
            return OffsetDateTime.parse(value.toString());
        } else if (fieldType.equals(List.class)) {
            return createList(value, itemType);
        } else if (Map.class.isAssignableFrom(fieldType)) {
            return createMap(value, itemType, valueType);
        } else if (Object.class.isAssignableFrom(fieldType)) {
            return createInstance(value, fieldType);
        }
        return null;
    }

    // Парсит список значений в список заданного типа
    private static List<Object> createList(Object valueObject, Class<?> itemType) throws Exception {
        if (!(valueObject instanceof List<?>)) {
            throw new IllegalArgumentException("Provided value is not a List: " + valueObject);
        }
        List<Object> values = (List<Object>) valueObject;
        List<Object> list = new ArrayList<>();
        for (Object value : values) {
            Object item = getValueByType(value, itemType, null, null); // Рекурсивно создаем объекты
            list.add(item);
        }
        return list;
    }

    private static Map<Object, Object> createMap(Object values, Class<?> keyType, Class<?> valueType) throws Exception {
        if (!(values instanceof HashMap<?, ?>)) {
            throw new IllegalArgumentException("Provided value is not a Map: " + values);
        }
        HashMap<?, ?> hashMap = (HashMap<?, ?>) values;
        Map<Object, Object> resultMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : hashMap.entrySet()) {
            Object key = getValueByType(entry.getKey(), keyType, null, null);
            Object value = getValueByType(entry.getValue(), valueType, null, null);
            resultMap.put(key, value);
        }
        return resultMap;
    }
}
