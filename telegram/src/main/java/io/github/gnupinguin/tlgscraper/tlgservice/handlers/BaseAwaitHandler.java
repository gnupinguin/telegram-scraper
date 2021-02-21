package io.github.gnupinguin.tlgscraper.tlgservice.handlers;

import io.github.gnupinguin.tlgscraper.tlgservice.handlers.awaitService.Either;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.concurrent.TimeoutException;

public abstract class BaseAwaitHandler<T extends TdApi.Object> implements Client.ResultHandler, Runnable {

    private static final long AWAIT_VALUE_STEP_TIME = 100;
    private static final long MINUTE = 1000 * 60;
    private static final long TIMEOUT_TIME = 5 * MINUTE;

    protected volatile Either result = null;

    @Override
    public void onResult(TdApi.Object object) {
        if (object instanceof TdApi.Error) {
            this.result = new Either(new RuntimeException(object.toString()));
            return;
        }
        this.result = new Either(object);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        while (result == null) {
            if (System.currentTimeMillis() - start > TIMEOUT_TIME) {
                result = new Either(new TimeoutException("Can not get Value: " + this));
            }
            try {
                Thread.sleep(AWAIT_VALUE_STEP_TIME);
            } catch (InterruptedException exp) {
                result = new Either(exp);
                return;
            }
        }
    }

    public T getResultOrThrow() {
        if (result == null) {
            throw new RuntimeException("Result is null (rather you had to wait for it)!");
        }
        if (result.exp != null) {
            throw new RuntimeException(result.exp);
        }
        return (T) result.data;
    }
}
