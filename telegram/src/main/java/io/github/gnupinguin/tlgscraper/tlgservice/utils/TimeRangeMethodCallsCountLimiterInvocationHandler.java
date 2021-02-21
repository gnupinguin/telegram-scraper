package io.github.gnupinguin.tlgscraper.tlgservice.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimeRangeMethodCallsCountLimiterInvocationHandler implements InvocationHandler {
    private final long timeRange;
    private final long countCallsLimit;
    private final Object original;

    private final Lock lock = new ReentrantLock();
    private long countCalls = 0;

    private volatile long startTime =  System.currentTimeMillis();

    public TimeRangeMethodCallsCountLimiterInvocationHandler(Object original, long timeRange, long countCallsLimit) {
        this.original = original;
        this.timeRange = timeRange;
        this.countCallsLimit = countCallsLimit;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final long currentRange = currentRangeMills();
        try {
            lock.lock();
            if (!isExpiredRange(currentRange)) {
                if (countCalls >= countCallsLimit) {
                    TimeUnit.MILLISECONDS.sleep(timeRange - currentRange);
                    countCalls = 0;
                    startTime = System.currentTimeMillis();
                }
            } else {
                startTime = System.currentTimeMillis();
                countCalls = 0;
            }
            countCalls++;
            return method.invoke(original, args);
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
