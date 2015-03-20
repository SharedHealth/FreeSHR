package org.freeshr.interfaces.encounter.ws.exceptions;

public class Forbidden extends RuntimeException {
    private String errorMessage;

    public Forbidden(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
