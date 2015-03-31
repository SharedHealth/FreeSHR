package org.freeshr.infrastructure.tr;


import rx.Observable;

public interface CodeValidator {
    boolean supports(String system);
    Observable<Boolean> isValid(String uri, String code);
}
