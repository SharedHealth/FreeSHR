package org.freeshr.utils;

import org.junit.Test;

import static org.freeshr.utils.StringUtils.concat;
import static org.freeshr.utils.StringUtils.ensureSuffix;
import static org.freeshr.utils.StringUtils.removeSuffix;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class StringUtilsTest {

    @Test
    public void shouldConcatStrings() {
        assertThat(concat("Test1", "Test2", "Test3"), is("Test1Test2Test3"));
        assertThat(concat("Test1", "", "Test3"), is("Test1Test3"));
        assertThat(concat("Test1", null, "Test3"), is("Test1Test3"));
        assertThat(concat(new String[]{}), is(""));
    }

    @Test
    public void shouldEnsureSuffix() {
        assertEquals("http://example.org/", ensureSuffix("http://example.org", "/"));
        assertEquals("1234", ensureSuffix("12", "34"));
    }

    @Test
    public void shouldRemoveSuffix() {
        assertEquals("http://example.org", removeSuffix("http://example.org/", "/"));
        assertEquals("12", removeSuffix("1234", "34"));
    }
}