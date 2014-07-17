package org.freeshr.utils;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {

    public static <I, O> List<O> map(List<I> xs, Fn<I, O> fn) {
        List<O> result = new ArrayList<O>();
        for (I input : xs) {
            result.add(fn.call(input));
        }
        return result;
    }

    public static interface Fn<I, O> {
        O call(I input);
    }
}
