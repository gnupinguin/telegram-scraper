package io.github.gnupinguin.tlgscraper.scraper;

import io.github.gnupinguin.tlgscraper.db.DbConfiguration;
import io.github.gnupinguin.tlgscraper.db.orm.DbProperties;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ManagedProxySelector;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ProxyProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.http.HttpClient;

@Import({DbConfiguration.class})
@Configuration
public class ScraperConfiguration {

    @Bean
    @ConfigurationProperties("db")
    public DbProperties dbProperties() {
        return new DbProperties();
    }

    @Bean
    public HttpClient httpClient(ProxyProvider proxyProvider) { //TODO consider extraction httpClient to extra class
        return HttpClient.newBuilder()
                .proxy(new ManagedProxySelector(proxyProvider))
                .build();
    }

}
