package io.github.gnupinguin.tlgscraper.scraper.proxy;

import java.util.Optional;

public interface ProxiedHttpClient {

    Optional<String> sendGet(String url);

}
