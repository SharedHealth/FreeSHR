package org.freeshr.shr.concurrent;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;

import java.util.concurrent.ExecutionException;

public class NotNull<T> extends ListenableFutureAdapter<Boolean, T> {

    public NotNull(ListenableFuture<T> adaptee) {
        super(adaptee);
    }

    @Override
    protected Boolean adapt(T result) throws ExecutionException {
        return null != result;
    }
}
