package io.github.gnupinguin.tlgscraper.scraper.proxy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Component
@ConditionalOnClass(TorConfiguration.class)
public class TorProxyProvider implements ProxyProvider {

    private final NativeCommandTorNymManager torProxySelector;
    private final Proxy torProxy;

    public TorProxyProvider(TorConfiguration torConfiguration, NativeCommandTorNymManager torProxySelector) {
        this.torProxySelector = torProxySelector;
        torProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(torConfiguration.getHost(), torConfiguration.getSocksPort()));
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
