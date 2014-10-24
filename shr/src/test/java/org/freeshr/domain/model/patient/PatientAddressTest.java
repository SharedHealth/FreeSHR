package org.freeshr.domain.model.patient;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PatientAddressTest {

    @Test
    public void shouldReturnConcatenatedAddressPArts() {
        Address address = new Address("01", "02", "03", "04", "05");
        assertEquals(address.getConcatenatedDistrictId(), "0102");
        assertEquals(address.getConcatenatedUpazilaId(), "010203");
        assertEquals(address.getConcatenatedCityCorporationId(), "01020304");
        assertEquals(address.getConcatenatedWardId(), "0102030405");
    }

    @Test
    public void shouldReturnNullForConcatenatedAddressPartIfIndividualPartIsNull() {
        Address address = new Address("01", "02", "03", null, null);
        assertNull(address.getConcatenatedCityCorporationId());
        assertNull(address.getConcatenatedWardId());
    }

}

