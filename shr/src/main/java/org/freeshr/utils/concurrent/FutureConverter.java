package org.freeshr.utils.concurrent;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public class FutureConverter {

    /**
     * Converts {@link org.springframework.util.concurrent.ListenableFuture} to  {@link rx.Observable}.
     *
     * @param listenableFuture
     * @param <T>
     * @return
     */
    public static <T> Observable<T> toObservable(ListenableFuture<T> listenableFuture) {
        if (listenableFuture instanceof ObservableListenableFuture) {
            return ((ObservableListenableFuture<T>) listenableFuture).getObservable();
        } else {
            return new ListenableFutureObservable<>(listenableFuture);
        }

    }

    /**
     * Converts  {@link rx.Observable} to {@link org.springframework.util.concurrent.ListenableFuture}.
     * Modifies the original Observable and takes only the first value.
     *
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> ListenableFuture<T> toListenableFuture(Observable<T> observable) {
        if (observable instanceof ListenableFutureObservable) {
            return ((ListenableFutureObservable<T>) observable).getListenableFuture();
        } else {
            return new ObservableListenableFuture<>(observable);
        }
    }

    static class ListenableFutureObservable<T> extends Observable<T> {
        private final ListenableFuture<T> listenableFuture;

        ListenableFutureObservable(ListenableFuture<T> listenableFuture) {
            super(onSubscribe(listenableFuture));
            this.listenableFuture = listenableFuture;
        }

        private static <T> OnSubscribe<T> onSubscribe(final ListenableFuture<T> listenableFuture) {
            return new OnSubscribe<T>() {
                @Override
                public void call(final Subscriber<? super T> subscriber) {
                    listenableFuture.addCallback(new ListenableFutureCallback<T>() {
                        @Override
                        public void onSuccess(T t) {
                            subscriber.onNext(t);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            subscriber.onError(throwable);
                        }
                    });
                    //listenable future is canceled upon unsubscribe
                    subscriber.add(Subscriptions.from(listenableFuture));
                }
            };
        }

        public ListenableFuture<T> getListenableFuture() {
            return listenableFuture;
        }
    }
}
