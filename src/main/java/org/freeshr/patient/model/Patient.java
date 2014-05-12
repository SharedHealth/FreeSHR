package org.freeshr.patient.model;


import java.util.Map;

public class Patient {

    private final boolean isNull;
    private final Profile profile;

    public Patient() {
        this.isNull = true;
        this.profile = null;
    }

    public Patient(Profile profile) {
        this.isNull = false;
        this.profile = profile;
    }

    public boolean isNull() {
        return isNull;
    }

    public Profile getProfile() {
        return profile;
    }
}
