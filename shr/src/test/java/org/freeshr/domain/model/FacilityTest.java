package org.freeshr.domain.model;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FacilityTest {

    @Test
    public void shouldSetCatchmentsGivenCommaSeparatedListOfCatchments() {
        Facility facility = new Facility();
        facility.setCatchments("01, 0203,09");
        assertThat(facility.getCatchments().containsAll(Arrays.asList("01", "0203", "09")), is(true));
    }

    @Test
    public void shouldGetCatchmentsAsCommaSeparatedListOfCatchments() {
        Facility facility = new Facility();
        facility.setCatchments("09,0989");
        assertThat(facility.getCatchmentsAsCommaSeparatedString(), is("09,0989"));
    }

    @Test
    public void shouldValidateFacilitiesCatchment() {
        Facility facility = new Facility();
        facility.setCatchments("09,0889");
        assertTrue(facility.has("09"));
        assertTrue(facility.has("0889"));
        assertTrue("catchments down the hierarchy should be valid", facility.has("088918"));
        assertFalse("catchments hierarchy should be validated", facility.has("078918"));
    }

}