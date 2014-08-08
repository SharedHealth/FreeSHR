package org.freeshr.utils;

public class Lambda {

    public static void throwIf(boolean condition, RuntimeException e) {
        if (condition) {
            throw e;
        }
    }

    public static void throwIfNot(boolean condition, RuntimeException e){
        if (!condition){
            throw e;
        }
    }
}
