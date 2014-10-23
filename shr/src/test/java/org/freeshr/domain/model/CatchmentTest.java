package org.freeshr.domain.model;

import org.freeshr.domain.model.Catchment;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CatchmentTest {

    @Test
    public void shouldParseCatchment() throws Exception {
        Catchment catchment = new Catchment("0102030405");
        assertEquals(5, catchment.getLevel());
        assertEquals(Catchment.UNION_OR_URBAN_WARD_ID, catchment.getType());

        catchment = new Catchment("");
        assertFalse("Should have identified catchment as invalid", catchment.isValid());

    }
}