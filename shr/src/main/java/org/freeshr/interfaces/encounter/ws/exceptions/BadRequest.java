package org.freeshr.interfaces.encounter.ws.exceptions;

public class BadRequest extends RuntimeException {

    private String errorMessage;

    public BadRequest(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
