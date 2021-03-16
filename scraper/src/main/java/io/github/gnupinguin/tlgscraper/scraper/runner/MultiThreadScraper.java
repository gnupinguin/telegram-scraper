package io.github.gnupinguin.tlgscraper.scraper.runner;

import io.github.gnupinguin.tlgscraper.scraper.notification.Notificator;
import io.github.gnupinguin.tlgscraper.scraper.scraper.CrossChatScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiThreadScraper {

    private final CrossChatScraper scrapper;
    private final Notificator notificator;
    private final ApplicationPoolConfiguration poolConfiguration;

    public void scrap() {
        ExecutorService pool = Executors.newFixedThreadPool(poolConfiguration.getPoolSize());
        IntStream.range(0, poolConfiguration.getPoolSize())
                .forEach(i -> pool.submit(() -> {
                    while (true) {
                        try {
                            scrapper.scrap();
                            return;
                        } catch (Exception e) {
                            log.info("Scraper exception", e);
                            notificator.sendException(e);
                        }
                    }
                }));
    }

}
