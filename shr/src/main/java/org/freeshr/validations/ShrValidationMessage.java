package org.freeshr.validations;

public class ShrValidationMessage {

    private Severity severity;
    private String location;
    private String type;
    private String message;


    public ShrValidationMessage(Severity severity, String location, String type, String message) {
        this.severity = severity;
        this.location = location;
        this.type = type;
        this.message = message;
    }

    public ShrValidationMessage() {
        this.severity = Severity.UNKNOWN;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
