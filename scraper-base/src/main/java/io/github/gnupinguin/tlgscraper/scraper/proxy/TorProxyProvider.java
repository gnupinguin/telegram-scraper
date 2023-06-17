package io.github.gnupinguin.tlgscraper.scraper.proxy;

import jakarta.annotation.Nonnull;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class TorProxyProvider implements ProxyProvider {

    private final NativeCommandTorNymManager torProxySelector;
    private final Proxy torProxy;

    public TorProxyProvider(TorConfiguration torConfiguration, NativeCommandTorNymManager torProxySelector) {
        this.torProxySelector = torProxySelector;
        torProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(torConfiguration.host(), torConfiguration.socksPort()));
    }

    @Nonnull
    @Override
    public Proxy next() {
        return torProxy;
    }

    @Override
    public boolean forceUpdate() {
        return torProxySelector.nextNode();
    }

}
