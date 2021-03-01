package io.github.gnupinguin.tlgscraper.scraper.telegram;

import java.util.function.Supplier;

public interface Limiter {

    <T> T withLimit(Supplier<T> handle);

}
