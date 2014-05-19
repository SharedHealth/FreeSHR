package org.freeshr.shr.concurrent;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class NotNullTest {

    @Test
    public void shouldReturnTrueOnAdaptWhenTheResolvedValueIsNotEmpty() throws ExecutionException, InterruptedException {
        assertTrue(new NotNull<String>(new PreResolvedListenableFuture<String>("nonEmptyValue")).get());
    }

    @Test
    public void shouldReturnTrueOnAdaptWhenTheResolvedValueIsEmpty() throws ExecutionException, InterruptedException {
        assertTrue(new NotNull<String>(new PreResolvedListenableFuture<String>("")).get());
    }

    @Test
    public void shouldReturnFalseOnAdaptWhenTheResolvedValueIsNull() throws ExecutionException, InterruptedException {
        assertFalse(new NotNull<String>(new PreResolvedListenableFuture<String>(null)).get());
    }
}