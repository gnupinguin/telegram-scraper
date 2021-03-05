package io.github.gnupinguin.tlgscraper.scraper.proxy;

import javax.annotation.Nonnull;
import java.net.Proxy;

public interface ProxySource {

    @Nonnull
    Proxy next();

    boolean forceUpdate();

}
