package com.vertex.pm.util;

import java.security.SecureRandom;

public final class IdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGenerator() {
    }

    /** Creates a readable random identifier such as PRJ-ABC123 for new rows. */
    public static String next(String prefix) {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder builder = new StringBuilder(prefix).append("-");
        for (int index = 0; index < 6; index++) {
            builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }
}
