package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import org.slf4j.Logger;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

public class RxMaps {

    public static <T> Func1<ResultSet, Observable<? extends T>> respondOnNext(final T value) {
        return new Func1<ResultSet, Observable<? extends T>>() {
            @Override
            public Observable<? extends T> call(ResultSet rows) {
                return Observable.just(value);
            }
        };
    }

    public static <T> Func0<Observable<? extends T>> completeResponds(final T value) {
        return new Func0<Observable<? extends T>>() {
            @Override
            public Observable<? extends T> call() {
                return Observable.just(value);
            }
        };
    }

    public static <T> Func1<Throwable, Observable<? extends T>> logAndForwardError(final Logger log) {
        return new Func1<Throwable, Observable<? extends T>>() {
            @Override
            public Observable<? extends T> call(Throwable throwable) {
                log.error(throwable.getMessage());
                return Observable.error(throwable);
            }
        };
    }
}
