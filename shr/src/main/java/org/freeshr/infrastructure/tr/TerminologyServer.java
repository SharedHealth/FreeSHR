package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

@Component
public class TerminologyServer {

    private final CodeValidatorFactory factory;
    private SHRProperties shrProperties;

    @Autowired
    public TerminologyServer(CodeValidatorFactory factory, SHRProperties shrProperties) {
        this.factory = factory;
        this.shrProperties = shrProperties;
    }

    public Observable<Boolean> isValid(String uri, String code) {
//        String trServerBaseUrl = shrProperties.getTerminologyServerReferencePath();
//        if (!uri.startsWith(trServerBaseUrl)) {
//            return Observable.just(false);
//        }

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
