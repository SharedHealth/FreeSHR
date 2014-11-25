package org.freeshr.interfaces.encounter.ws;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.freeshr.application.fhir.Error;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorInfo implements Serializable {
    @JsonProperty
    private String httpStatus;
    @JsonProperty
    private String message;
    @JsonProperty
    private List<Error> errors;

    public ErrorInfo(HttpStatus httpStatus, Exception ex) {
        this.httpStatus = httpStatus.toString();
        this.message = ex.getLocalizedMessage();
    }

    public ErrorInfo(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus.toString();
        this.message = message;
    }


    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }
}