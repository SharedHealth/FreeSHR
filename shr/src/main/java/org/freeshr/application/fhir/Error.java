package org.freeshr.application.fhir;


import com.google.gson.Gson;

public class Error {

    private String code;
    private String reason;

    public Error() {
    }

    public Error(String code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
