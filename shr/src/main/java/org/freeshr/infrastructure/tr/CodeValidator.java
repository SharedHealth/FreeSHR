package org.freeshr.infrastructure.tr;


import org.springframework.util.concurrent.ListenableFuture;

public interface CodeValidator {
    ListenableFuture<Boolean> isValid(String uri, String code);
}
