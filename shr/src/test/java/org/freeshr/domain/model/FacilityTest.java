package org.freeshr.domain.model;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FacilityTest {

    @Test
    public void shouldSetCatchmentsGivenCommaSeparatedListOfCatchments(){
        Facility facility = new Facility();
        facility.setCatchments("01, 0203,09");
        assertThat(facility.getCatchments().containsAll(Arrays.asList("01", "0203", "09")), is(true));
    }

    @Test
    public void shouldGetCatchmentsAsCommaSeparatedListOfCatchments(){
        Facility facility = new Facility();
        facility.setCatchments("09,0989");
        assertThat(facility.getCatchmentsAsCommaSeparatedString(), is("09,0989"));
    }

}