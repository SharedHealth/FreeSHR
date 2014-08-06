package org.freeshr.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.freeshr.utils.CollectionUtils.fetch;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

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


}