package io.github.gnupinguin.tlgscraper.scraper.proxy;


import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
public class ManagedProxySelector extends ProxySelector {

    private final ProxyProvider proxyProvider;

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException e) {
        //ignore
    }

    @Override
    public synchronized List<Proxy> select(URI uri) {
        return List.of(proxyProvider.next());
    }

}
