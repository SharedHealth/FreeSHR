package org.freeshr.utils;

import org.junit.Test;

import static org.freeshr.utils.StringUtils.concat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StringUtilsTest {

    @Test
    public void shouldConcatStrings() {
       assertThat(concat("Test1", "Test2", "Test3"), is("Test1Test2Test3"));
       assertThat(concat("Test1", "", "Test3"), is("Test1Test3"));
       assertThat(concat("Test1", null, "Test3"), is("Test1Test3"));
       assertThat(concat(new String[]{}), is(""));
    }
}