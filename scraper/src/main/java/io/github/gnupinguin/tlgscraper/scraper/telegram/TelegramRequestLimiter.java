package io.github.gnupinguin.tlgscraper.scraper.telegram;

import java.util.function.Supplier;

public interface TelegramRequestLimiter {

    <T> T withLimit(Supplier<T> handle);

}
