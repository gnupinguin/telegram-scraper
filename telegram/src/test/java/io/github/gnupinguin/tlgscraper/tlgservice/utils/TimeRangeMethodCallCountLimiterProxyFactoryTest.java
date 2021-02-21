package io.github.gnupinguin.tlgscraper.tlgservice.utils;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.gnupinguin.tlgscraper.tlgservice.utils.TimeRangeMethodCallCountLimiterProxyFactory.getProxy;
import static org.junit.Assert.assertEquals;

public class TimeRangeMethodCallCountLimiterProxyFactoryTest {

    private interface Counter {
        int get();

        void increment();
    }

    private static class CounterImpl implements Counter {

        private final AtomicInteger counter = new AtomicInteger(0);

        public int get() {
            return counter.get();
        }

        public void increment() {
            counter.incrementAndGet();
        }
    }

    @Test
    public void testProxyPublicMethodCallsAreLimitedBy15PerSec() throws Exception {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Counter counter = getProxy(new CounterImpl(), Counter.class);
        final long start = System.currentTimeMillis();
        executorService.submit(() -> {
                while ((System.currentTimeMillis() - start) < 200) {
                    counter.increment();
                }
        });
        executorService.awaitTermination(200, TimeUnit.MILLISECONDS);
        assertEquals(2 + 1, counter.get());
    }
}