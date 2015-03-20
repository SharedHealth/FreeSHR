package org.freeshr.interfaces.encounter.ws.exceptions;

public class Forbidden extends RuntimeException {
    private String errorMessage;

    public Forbidden(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
