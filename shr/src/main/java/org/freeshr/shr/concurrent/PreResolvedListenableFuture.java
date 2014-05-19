package org.freeshr.shr.concurrent;

import org.springframework.util.concurrent.ListenableFutureTask;

import java.util.concurrent.Callable;

/**
 * Helps create methods which return a ListenableFuture in one case and a resolved value in another
 *
 * @param <T> Type of the value promised
 */
public class PreResolvedListenableFuture<T> extends ListenableFutureTask<T> {

    public PreResolvedListenableFuture(final T value) {
        super(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return value;
            }
        });
        set(value);
    }
}
