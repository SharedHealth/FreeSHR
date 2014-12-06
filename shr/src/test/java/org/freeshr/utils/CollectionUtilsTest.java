package org.freeshr.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.freeshr.utils.CollectionUtils.fetch;
import static org.freeshr.utils.CollectionUtils.toSet;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CollectionUtilsTest {

    @Test
    public void shouldFetchValueFromANestedMap() throws Exception {
        Map<String, Object> cmap = new HashMap<String, Object>();
        Map<String, Object> bmap = new HashMap<String, Object>();
        Map<String, Object> amap = new HashMap<String, Object>();
        cmap.put("c", "test");
        bmap.put("b", cmap);
        amap.put("a", bmap);
        assertThat(fetch(amap, "a.b.c").toString(), is("test"));
    }

    @Test
    public void shouldConvertCommaSeparetedStringToSet() throws Exception {
        assertTrue(toSet("sun, moon, star", ",").containsAll(asList("sun", "moon", "star")));
        assertTrue(toSet("", ",").isEmpty());
        assertTrue(toSet(null, ",").isEmpty());
    }

}