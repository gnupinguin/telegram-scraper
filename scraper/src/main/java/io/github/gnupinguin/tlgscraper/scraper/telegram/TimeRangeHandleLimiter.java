package io.github.gnupinguin.tlgscraper.scraper.telegram;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class TimeRangeHandleLimiter implements Limiter {

    private final Lock lock = new ReentrantLock();
    private volatile boolean methodWasCalled = false;
    private volatile long startTime =  System.currentTimeMillis();

    private final long timeRange;

    @Override
    public <T> T withLimit(Supplier<T> handle) {
        final long currentRange = currentRangeMills();
        try {
            lock.lock();
            if (!isExpiredRange(currentRange)) {
                if (methodWasCalled) {
                    TimeUnit.MILLISECONDS.sleep(timeRange - currentRange);
                    methodWasCalled = false;
                    startTime = System.currentTimeMillis();
                }
            } else {
                startTime = System.currentTimeMillis();
                methodWasCalled = false;
            }
            methodWasCalled = true;
            return handle.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private boolean isExpiredRange(long currentRange) {
        return currentRange > timeRange;
    }

    private long currentRangeMills() {
        return System.currentTimeMillis() - startTime;
    }

}
