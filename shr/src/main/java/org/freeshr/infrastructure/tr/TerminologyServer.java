package org.freeshr.infrastructure.tr;

import org.freeshr.utils.concurrent.PreResolvedListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
public class TerminologyServer {

    private final CodeValidatorFactory factory;

    @Autowired
    public TerminologyServer(CodeValidatorFactory factory) {
        this.factory = factory;
    }

    public ListenableFuture<Boolean> isValid(String uri, String code) {
        if (factory.getValidator(uri) != null) {
            return factory.getValidator(uri).isValid(uri, code);
        }
        return new PreResolvedListenableFuture<>(false);
    }
}
