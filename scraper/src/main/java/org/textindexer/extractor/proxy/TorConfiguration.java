package org.textindexer.extractor.proxy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties("tor")
public class TorConfiguration {

    private final String host;

    private final int socksPort;

    private final int httpPort;

    private final int controlPort;

    private final String password;

}
