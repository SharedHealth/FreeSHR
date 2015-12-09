package org.freeshr.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeUuidUtilTest {

    @Test
    public void shouldInvalidateAWrongUUID() throws Exception {
        assertFalse(TimeUuidUtil.isValidTimeUUID("invalid-uuid"));
    }

    @Test
    public void shouldInvalidateAWrongTimeUUID() throws Exception {
        assertFalse(TimeUuidUtil.isValidTimeUUID("4549c469-a442-4e94-9eff-834a69d678d7"));
    }

    @Test
    public void shouldValidateATimeUUID() throws Exception {
        assertTrue(TimeUuidUtil.isValidTimeUUID("94a77550-9e46-11e5-c000-000000000000"));

    }
}