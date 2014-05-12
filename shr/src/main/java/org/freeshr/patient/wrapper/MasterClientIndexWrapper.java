package org.freeshr.patient.wrapper;

import org.springframework.stereotype.Component;

@Component
public class MasterClientIndexWrapper {

    public Boolean isValid(String healthId) {
        return Boolean.TRUE;
    }
}
