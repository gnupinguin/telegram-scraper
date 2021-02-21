package io.github.gnupinguin.tlgscraper.tlgservice.handlers.awaitService;

import io.github.gnupinguin.tlgscraper.tlgservice.handlers.BaseAwaitHandler;
import org.drinkless.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HandlersAwaitService {

    private static final Logger log = LoggerFactory.getLogger(HandlersAwaitService.class);


    private static final int THREAD_NUMBER = 2;
    private final ExecutorService executorService
            = Executors.newFixedThreadPool(THREAD_NUMBER);

    public Future<?> getFuture(BaseAwaitHandler handler) {
        return executorService.submit(handler);
    }

    public <T extends TdApi.Object> T get(BaseAwaitHandler<T> handler) {
        try {
            getFuture(handler).get();
        } catch (Exception exp) {
            log.info("Telegram error happened");
            System.exit(1);//TODO fix it
            throw new RuntimeException(exp);
        }
        return handler.getResultOrThrow();
    }
}
