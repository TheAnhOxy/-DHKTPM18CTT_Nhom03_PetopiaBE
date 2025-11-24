package com.pet.utils;

public class StringUtil {

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
