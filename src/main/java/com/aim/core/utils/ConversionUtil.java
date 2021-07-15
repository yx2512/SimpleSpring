package com.aim.core.utils;

import com.aim.mvc.exception.TypeConversionException;

public class ConversionUtil {
    public static Object primitiveNull(Class<?> type) {
        if(type.equals(int.class) || type.equals(double.class) || type.equals(long.class) || type.equals(Integer.class) || type.equals(Double.class) || type.equals(Long.class)) {
            return 0;
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return false;
        } else if (type.equals(String.class)) {
            return "";
        }
        return null;
    }

    public static Object convert(Class<?> type, String value) {
        if(!type.isPrimitive() && !type.equals(String.class)) {
            throw new TypeConversionException("Does not support non-primitive type yet");
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
                throw new TypeConversionException("Does not support non-primitive yet");
            }
        }
    }
}
