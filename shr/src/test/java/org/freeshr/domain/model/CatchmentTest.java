package org.freeshr.domain.model;

import org.junit.Test;
import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CatchmentTest {

    @Test
    public void shouldParseCatchment() throws Exception {


        Observable<Integer> nocache = Observable.just(1).cache();
        Observable<Integer> cache = Observable.just(1).cache();

        assertEquals(1, nocache.toBlocking().first().intValue());
        assertEquals(1, nocache.toBlocking().first().intValue());

        assertEquals(1, cache.toBlocking().first().intValue());
        assertEquals(1, cache.toBlocking().first().intValue());



        Catchment catchment = new Catchment("0102030405");
        assertEquals(5, catchment.getLevel());
        assertEquals(Catchment.UNION_OR_URBAN_WARD_ID, catchment.getType());

        catchment = new Catchment("");
        assertFalse("Should have identified catchment as invalid", catchment.isValid());

    }
}