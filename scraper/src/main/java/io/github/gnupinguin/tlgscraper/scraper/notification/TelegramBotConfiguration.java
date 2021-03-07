package io.github.gnupinguin.tlgscraper.scraper.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@ConfigurationProperties("telegram.bot")
@RequiredArgsConstructor
public class TelegramBotConfiguration {

    private final String token;

    private final long chatId;

}
