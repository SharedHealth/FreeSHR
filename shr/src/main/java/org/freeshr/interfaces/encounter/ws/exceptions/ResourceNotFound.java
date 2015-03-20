package org.freeshr.interfaces.encounter.ws.exceptions;

public class ResourceNotFound extends RuntimeException {

    private String errorMessage;

    public ResourceNotFound(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
