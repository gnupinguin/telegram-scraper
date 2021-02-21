package org.textindexer.extractor.proxy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Component
@ConditionalOnProperty(prefix = "proxy", value = "circleList")
public class CircleProxySource implements ProxySource {

    private final List<Proxy> proxies;

    private final AtomicInteger currentProxy = new AtomicInteger();

    public CircleProxySource(CircleProxiesConfiguration circleProxies) {
        List<Proxy> circleList = circleProxies.getCircleList();
        this.proxies = circleList.isEmpty() ? List.of(Proxy.NO_PROXY) : circleList;
    }

    @Override
    @Nonnull
    public Proxy next() {
        return proxies.get(currentProxy.get());
    }

    @Override
    public void forceUpdate() {
        int next = currentProxy.get() + 1;
        if (next >= proxies.size()) {
            next = 0;
        }
        currentProxy.set(next);
    }

}
