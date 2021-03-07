package io.github.gnupinguin.tlgscraper.scraper.scraper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties("scraper")
public class ScraperConfiguration {

    private final int messagesCount;

    private final int maxFailures;

}
