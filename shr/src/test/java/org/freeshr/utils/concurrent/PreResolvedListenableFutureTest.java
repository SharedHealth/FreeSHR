package org.freeshr.utils.concurrent;

import org.freeshr.util.ResultHolder;
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PreResolvedListenableFutureTest {

    @Test
    public void shouldReturnTheResolvedValueOnGet() throws ExecutionException, InterruptedException {
        assertTrue(new PreResolvedListenableFuture<Boolean>(true).get());
    }

    @Test
    public void shouldBeAbleToResolveValueOfAnyType() throws ExecutionException, InterruptedException {
        String testString = "test";
        assertEquals(testString, new PreResolvedListenableFuture<String>(testString).get());
    }

    @Test
    public void shouldInvokeCallbackAsSoonAsItIsAdded() {
        final ResultHolder resultHolder = new ResultHolder(false);
        PreResolvedListenableFuture<Boolean> future = new PreResolvedListenableFuture<Boolean>(true);
        future.addCallback(new ListenableFutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                resultHolder.setCalled(true);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
        assertTrue(future.isDone());
        assertTrue(resultHolder.getCalled());
    }
}