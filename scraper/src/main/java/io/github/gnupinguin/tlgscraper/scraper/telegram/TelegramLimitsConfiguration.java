package io.github.gnupinguin.tlgscraper.scraper.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@ConfigurationProperties("telegram.limits")
@RequiredArgsConstructor
public class TelegramLimitsConfiguration {

    private final long minTimeRangeMs;

    private final long maxTimeRangeMs;

}
