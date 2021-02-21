package org.textindexer.extractor.proxy;

import javax.annotation.Nonnull;
import java.net.Proxy;

public interface ProxySource {

    @Nonnull
    Proxy next();

    void forceUpdate();

}
