package io.github.gnupinguin.tlgscraper.scraper;

import com.pengrad.telegrambot.TelegramBot;
import io.github.gnupinguin.tlgscraper.scraper.notification.TelegramBotConfiguration;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ManagedProxySelector;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ProxyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class ScraperConfiguration {

    @Bean
    public HttpClient httpClient(ProxyProvider proxyProvider) { //TODO consider extraction httpClient to extra class
        return HttpClient.newBuilder()
                .proxy(new ManagedProxySelector(proxyProvider))
                .build();
    }

    @Bean
    public TelegramBot bot(TelegramBotConfiguration configuration) {
        return new TelegramBot(configuration.getToken());
    }

}
