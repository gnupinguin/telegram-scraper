package io.github.gnupinguin.tlgscraper.scraper.proxy;

import jakarta.annotation.Nonnull;

import java.net.Proxy;

public interface ProxyProvider {

    @Nonnull
    Proxy next();

    boolean forceUpdate();

}
