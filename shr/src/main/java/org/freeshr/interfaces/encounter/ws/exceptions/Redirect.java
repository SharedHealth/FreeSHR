package org.freeshr.interfaces.encounter.ws.exceptions;

public class Redirect extends RuntimeException {
    private String errorMessage;

    public Redirect(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
