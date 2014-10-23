package org.freeshr.domain.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FacilityCatchmentTest {

    @Test
    public void shouldParseCatchment() throws Exception {
        String catchment = "0102030405";
        FacilityCatchment facilityCatchment = new FacilityCatchment(catchment);



        assertEquals(5, facilityCatchment.getLevel());
        assertEquals(FacilityCatchment.UNION_OR_URBAN_WARD_ID, facilityCatchment.getCatchmentType());

    }
}