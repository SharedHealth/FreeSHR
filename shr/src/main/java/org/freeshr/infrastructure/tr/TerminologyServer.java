package org.freeshr.infrastructure.tr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

@Component
public class TerminologyServer {

    private final CodeValidatorFactory factory;

    @Autowired
    public TerminologyServer(CodeValidatorFactory factory) {
        this.factory = factory;
    }

    public Observable<Boolean> isValid(String uri, String code) {
        CodeValidator validator = factory.getValidator(uri);
        if (validator != null) {
            return validator.isValid(uri, code);
        }
        return Observable.just(false);
    }

    public boolean verifiesSystem(String system) {
        return factory.getValidator(system) != null;
    }
}
