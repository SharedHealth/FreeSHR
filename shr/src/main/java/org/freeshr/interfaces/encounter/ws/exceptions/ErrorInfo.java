package org.freeshr.interfaces.encounter.ws.exceptions;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.freeshr.application.fhir.Error;
import org.springframework.http.HttpStatus;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement
public class ErrorInfo implements Serializable {
    @JsonProperty
    @XmlElement
    private String httpStatus;
    @JsonProperty
    @XmlElement
    private String message;
    @JsonProperty
    @XmlElement
    private List<Error> errors;

    public ErrorInfo() {
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.toString();
    }

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