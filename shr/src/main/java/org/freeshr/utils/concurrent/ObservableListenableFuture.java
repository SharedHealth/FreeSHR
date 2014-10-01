package org.freeshr.utils.concurrent;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.*;
import rx.Observable;
import rx.functions.Action1;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ObservableListenableFuture<T> implements ListenableFuture<T> {
    private final Observable<T> observable;
    private final Future<T> futureFromObservable;
    private final ListenableFutureCallbackRegistry<T> callbackRegistry = new ListenableFutureCallbackRegistry<>();

    ObservableListenableFuture(Observable<T> wrapped) {
        this.observable = wrapped.asObservable();
        this.futureFromObservable = wrapped
                .doOnNext(new Action1<T>() {
                    @Override
                    public void call(T t) {
                        callbackRegistry.success(t);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callbackRegistry.failure(throwable);
                    }
                })
                .toBlockingObservable().toFuture();
    }

    @Override
    public void addCallback(ListenableFutureCallback<? super T> callback) {
        callbackRegistry.addCallback(callback);
    }

    @Override
    public void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback) {

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureFromObservable.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return futureFromObservable.isCancelled();
    }

    @Override
    public boolean isDone() {
        return futureFromObservable.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return futureFromObservable.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return futureFromObservable.get(timeout, unit);
    }

    public Observable<T> getObservable() {
        return observable;
    }
}
