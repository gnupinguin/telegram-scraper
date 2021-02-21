package org.textindexer.extractor.proxy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.net.Proxy;
import java.util.List;

@Getter
@ConstructorBinding
@ConfigurationProperties("proxy")
@RequiredArgsConstructor
public class CircleProxiesConfiguration {

    private List<Proxy> circleList;
}
