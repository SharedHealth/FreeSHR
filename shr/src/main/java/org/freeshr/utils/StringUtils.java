package org.freeshr.utils;


import static org.freeshr.utils.CollectionUtils.ReduceFn;
import static org.freeshr.utils.CollectionUtils.reduce;

public class StringUtils {
    public static String concat(String... xs) {
        StringBuilder builder = reduce(xs, new StringBuilder(), new ReduceFn<String, StringBuilder>() {
            @Override
            public StringBuilder call(String input, StringBuilder acc) {
                return acc.append(input);
            }
        });
        return builder.toString();
    }

    public static String ensureSuffix(String value, String pattern) {
        String trimmedValue = value.trim();
        if (trimmedValue.endsWith(pattern)) {
            return trimmedValue;
        } else {
            return trimmedValue + pattern;
        }
    }

    public static String removeSuffix(String value, String pattern) {
        String trimmedValue = value.trim();
        if (trimmedValue.endsWith(pattern)) {
            return trimmedValue.substring(0, trimmedValue.lastIndexOf(pattern));
        } else {
            return trimmedValue;
        }
    }
}
