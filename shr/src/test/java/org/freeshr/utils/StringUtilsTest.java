package org.freeshr.utils;

import org.junit.Test;

import static org.freeshr.utils.StringUtils.concat;
import static org.freeshr.utils.StringUtils.ensureEndsWithBackSlash;
import static org.freeshr.utils.StringUtils.removeEndBackSlash;
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
    public void shouldEnsureBackSlashAtEnd() {
        assertEquals("http://example.org/", ensureEndsWithBackSlash("http://example.org"));
    }

    @Test
    public void shouldRemoveEndingBackSlash() {
        assertEquals("http://example.org", removeEndBackSlash("http://example.org/"));
    }
}