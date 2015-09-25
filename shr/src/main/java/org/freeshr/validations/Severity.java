package org.freeshr.validations;

public enum Severity {
    FATAL("fatal"),
    ERROR("error"),
    WARNING("warning"),
    INFORMATION("information"),
    UNKNOWN("unknown");

    private Severity(String theCode) {
        myCode = theCode;
    }

    public String getCode() {
        return myCode;
    }

    private String myCode;
}
