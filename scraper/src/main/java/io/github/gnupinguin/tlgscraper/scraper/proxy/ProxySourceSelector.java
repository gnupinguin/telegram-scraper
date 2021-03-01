package io.github.gnupinguin.tlgscraper.scraper.proxy;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProxySourceSelector extends ProxySelector {

    private final ProxySource proxySource;

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException e) {
        proxySource.forceUpdate();
    }

    @Override
    public synchronized List<Proxy> select(URI uri) {
        return List.of(proxySource.next());
    }

}
