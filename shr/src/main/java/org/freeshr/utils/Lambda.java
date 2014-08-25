package org.freeshr.utils;

public class Lambda {

    public static void throwIfNot(boolean condition, RuntimeException e) {
        if (!condition) {
            throw e;
        }
    }
}
