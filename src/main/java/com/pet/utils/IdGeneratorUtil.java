package com.pet.utils;

import java.util.UUID;

public class IdGeneratorUtil {

    public static String generateArticleId() {
        return "AR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    public static String generateCommentId() {
        return "AC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
