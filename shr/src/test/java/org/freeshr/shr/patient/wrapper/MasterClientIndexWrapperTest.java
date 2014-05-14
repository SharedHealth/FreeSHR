package org.freeshr.shr.patient.wrapper;

import org.freeshr.shr.config.EnvironmentMock;
import org.freeshr.shr.config.SHRConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(initializers = EnvironmentMock.class, classes = SHRConfig.class)
public class MasterClientIndexWrapperTest {

    private final String validHealthId = "10";
    private final String invalidHealthId = "20";

    @Autowired
    private MasterClientIndexWrapper masterClientIndexWrapper;

    @Test
    public void shouldReturnTrueWhenHealthIdIsValid() {
        assertTrue(masterClientIndexWrapper.isValid(validHealthId));
    }

    @Test
    public void shouldReturnFalseWhenHealthIdIsNotValid() {
        assertFalse(masterClientIndexWrapper.isValid(invalidHealthId));
    }

}