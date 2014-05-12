package org.freeshr.patient.model;

import java.util.Date;

public class Profile {
    private String HID;
    private String nationality;
    private String bloodGroup;
    private String placeOfBirth;
    private String firstName;
    private String middleName;
    private String lastName;
    private String dateOfBirth;
    private String address;
    private String gender;
    private String maritalStatus;
    private Date dateCreated;
    private Date dateModified;

    public String getHID() {
        return HID;
    }

    public void setHID(String HID) {
        this.HID = HID;
    }
}
