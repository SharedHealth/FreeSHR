package org.freeshr.infrastructure.tr;


import rx.Observable;

public interface CodeValidator {
    Observable<Boolean> isValid(String uri, String code);
}
