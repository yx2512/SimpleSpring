package com.simplespring.core.utils;

public class ConversionUtil {
    public static Object primitiveNull(Class<?> type) {
        if(type == int.class || type == double.class ||
                type == short.class || type == long.class ||
                type == byte.class || type == float.class) {
            return 0;
        } else if (type == boolean.class) {
            return false;
        } else if (type == String.class) {
            return "";
        }
        return null;
    }

    public static Object convert(Class<?> type, String value) {
        if(!type.isPrimitive()) {
            throw new RuntimeException("Does not support non-primitive type yet");
        } else {
            if(value == null) {
                return primitiveNull(type);
            } else {
                if (Integer.class.equals(type) || int.class.equals(type)) {
                    return Integer.parseInt(value);
                } else if (Double.class.equals(type) || double.class.equals(type)) {
                    return Double.parseDouble(value);
                } else if (String.class.equals(type)) {
                    return value;
                } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
                    return Boolean.getBoolean(value);
                }
                throw new RuntimeException("Does not support non-primitive yet");
            }
        }
    }
}
