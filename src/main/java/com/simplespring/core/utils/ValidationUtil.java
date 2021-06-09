package com.simplespring.core.utils;

import java.util.Collection;
import java.util.Set;

public class ValidationUtil {

    public static boolean NotNullOrEmpty(Collection <?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
