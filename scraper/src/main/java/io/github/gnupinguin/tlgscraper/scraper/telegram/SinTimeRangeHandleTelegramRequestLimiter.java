package io.github.gnupinguin.tlgscraper.scraper.telegram;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class SinTimeRangeHandleTelegramRequestLimiter implements TelegramRequestLimiter {

    private final Lock lock = new ReentrantLock();
    private volatile boolean methodWasCalled = false;
    private volatile long startTime = System.currentTimeMillis();

    private final long timeRangeMin;
    private final long timeRangeStep;

    public SinTimeRangeHandleTelegramRequestLimiter(TelegramLimitsConfiguration configuration) {
        this.timeRangeMin = configuration.getMinTimeRangeMs();
        this.timeRangeStep = configuration.getMaxTimeRangeMs() - timeRangeMin;
        assert timeRangeStep >= 0;
    }

    @Override
    public <T> T withLimit(Supplier<T> handle) {
        try {
            lock.lock();
            long awaiting = calculateAwaiting();
            if (awaiting > 0) {
                if (methodWasCalled) {
                    TimeUnit.MILLISECONDS.sleep(awaiting);
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

    private long calculateAwaiting(){
        long now = System.currentTimeMillis();
        long end = startTime + timeRangeMin + Math.round(Math.abs(Math.sin(now)) * timeRangeStep);
        return end - now;
    }

}
