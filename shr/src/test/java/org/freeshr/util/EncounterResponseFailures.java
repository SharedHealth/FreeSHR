package org.freeshr.util;


import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterResponse;

public class EncounterResponseFailures {

    private EncounterResponse response;

    public EncounterResponseFailures(EncounterResponse response) {
        this.response = response;
    }

    public boolean matches(String[]... expectedErrors) {
        return isNotSuccessful() && hasErrorMessages(expectedErrors);
    }

    private boolean hasErrorMessages(String[][] expectedErrors) {
        boolean result = expectedErrors.length == response.getErrors().size();
        for (int i = 0; result && i < expectedErrors.length; i++) {
            String[] expectedError = expectedErrors[i];
            result = StringUtils.equals(expectedError[0], response.getErrors().get(i).getField())
                    && StringUtils.equals(expectedError[1], response.getErrors().get(i).getType())
                    && StringUtils.equals(expectedError[2], response.getErrors().get(i).getReason());
        }
        return result;
    }

    private boolean isNotSuccessful() {
        return !response.isSuccessful();
    }
}
