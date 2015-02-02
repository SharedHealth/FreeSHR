package org.freeshr.application.fhir;


import com.google.gson.Gson;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class Error implements Serializable {

    @XmlElement
    private String field;
    @XmlElement
    private String type;
    @XmlElement
    private String reason;

    public Error() {
    }

    public Error(String field, String type, String reason) {
        this.field = field;
        this.type = type;
        this.reason = reason;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

}