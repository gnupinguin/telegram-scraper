package io.github.gnupinguin.tlgscraper.scraper.runner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties("app.pool")
public class ApplicationPoolConfiguration {

    private final int poolSize;

}
