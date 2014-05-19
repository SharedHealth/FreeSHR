package org.freeshr.shr.concurrent;

import org.springframework.util.concurrent.ListenableFutureTask;

import java.util.concurrent.Callable;

public class PreResolvedListenableFuture<T> extends ListenableFutureTask<T> {

    public PreResolvedListenableFuture(final T value) {
        super(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return value;
            }
        });
    }
}
