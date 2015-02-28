package org.freeshr.utils;


import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class CollectionUtils {

    public static <I, O> List<O> map(List<I> xs, Fn<I, O> fn) {
        List<O> result = new ArrayList<O>();
        for (I input : xs) {
            result.add(fn.call(input));
        }
        return result;
    }

    public static <I, O> Set<O> mapToSet(I[] xs, Fn<I, O> fn) {
        Set<O> result = new HashSet<O>();
        for (I input : xs) {
            result.add(fn.call(input));
        }
        return result;
    }

    public static <I, O> O reduce(I[] xs, O acc, ReduceFn<I, O> fn) {
        return reduce(toList(xs), acc, fn);
    }

    public static <I, O> O reduce(List<I> xs, O acc, ReduceFn<I, O> fn) {
        O result = acc;
        for (I x : xs) {
            if (x != null) {
                result = fn.call(x, result);
            }
        }
        return result;
    }

    public static <I> List<I> toList(I... xs) {
        if (isNotEmpty(xs)) {
            return Arrays.asList(xs);
        }
        return Collections.emptyList();
    }

    private static <I> boolean isNotEmpty(I[] xs) {
        return xs != null && xs.length > 0;
    }

    public static <I> List<I> filter(List<I> xs, Fn<I, Boolean> fn) {
        List<I> result = new ArrayList<I>();
        for (I x : xs) {
            if (fn.call(x)) {
                result.add(x);
            }
        }
        return result;
    }

    public static <I> I find(List<I> xs, Fn<I, Boolean> fn) {
        for (I x : xs) {
            if (fn.call(x)) {
                return x;
            }
        }
        return null;
    }

    public static <I> boolean isNotEmpty(List<I> xs) {
        return xs != null && xs.size() > 0;
    }

    public static <I> boolean isEmpty(List<I> xs) {
        return xs == null || xs.size() == 0;
    }

    public static <I> I first(List<I> xs) {
        return isNotEmpty(xs) ? xs.get(0) : null;
    }

    public static <I> boolean isEvery(List<I> xs, Fn<I, Boolean> fn) {
        for (I x : xs) {
            if (!fn.call(x)) {
                return false;
            }
        }
        return true;
    }

    public static <I> void forEach(List<I> xs, Fn<I, Boolean> fn) {
        for (I x : xs) {
            fn.call(x);
        }
    }

    public static <I> Fn<I, Boolean> not(final Fn<I, Boolean> fn) {
        return new Fn<I, Boolean>() {
            @Override
            public Boolean call(I input) {
                return !fn.call(input);
            }
        };
    }

    public static <I> boolean isAny(List<I> xs, Fn<I, Boolean> fn) {
        for (I x : xs) {
            if (fn.call(x)) {
                return true;
            }
        }
        return false;
    }


    public static interface Fn<I, O> {
        O call(I input);
    }

    public static interface ReduceFn<I, O> {
        O call(I input, O acc);
    }

    public static class And<I> implements Fn<I, Boolean> {

        private final Fn<I, Boolean> fn1;
        private final Fn<I, Boolean> fn2;

        public And(Fn<I, Boolean> fn1, Fn<I, Boolean> fn2) {
            this.fn1 = fn1;
            this.fn2 = fn2;
        }

        @Override
        public Boolean call(I input) {
            return fn1.call(input) && fn2.call(input);
        }
    }

    public static Object fetch(Map map, String path) {
        return reduce(split(path, "."), map, new ReduceFn<String, Object>() {
            @Override
            public Object call(String input, Object acc) {
                if (acc == null) {
                    return EMPTY;
                }
                if (acc instanceof Map) {
                    Map map = (Map) acc;
                    return map.containsKey(input) ? map.get(input) : null;
                }
                return acc;
            }
        });

    }

    public static Set<String> toSet(String str, String seperator) {
        return mapToSet(split(defaultIfEmpty(str, EMPTY), seperator), new Fn<String, String>() {
            public String call(String input) {
                return strip(input);
            }
        });
    }

}
