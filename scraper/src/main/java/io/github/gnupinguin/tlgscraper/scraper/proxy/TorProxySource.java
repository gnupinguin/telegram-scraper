package io.github.gnupinguin.tlgscraper.scraper.proxy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Component
@ConditionalOnClass(TorConfiguration.class)
public class TorProxySource implements ProxySource {

    private final NativeCommandTorNymManager torProxySelector;
    private final Proxy torProxy;

    public TorProxySource(TorConfiguration torConfiguration, NativeCommandTorNymManager torProxySelector) {
        this.torProxySelector = torProxySelector;
        torProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(torConfiguration.getHost(), torConfiguration.getSocksPort()));
    }

    @Nonnull
    @Override
    public Proxy next() {
        return torProxy;
    }

    @Override
    public void forceUpdate() {
        torProxySelector.nextNode();
    }

}
