package org.textindexer.extractor;

import io.github.gnupinguin.tlgscraper.db.DbConfiguration;
import io.github.gnupinguin.tlgscraper.db.orm.DbProperties;
import io.github.gnupinguin.tlgscraper.tlgservice.TlgConfiguration;
import io.github.gnupinguin.tlgscraper.tlgservice.config.TlgClientProperties;
import io.github.gnupinguin.tlgscraper.tlgservice.config.TlgLoginProperties;
import io.github.gnupinguin.tlgscraper.tlgservice.telegram.TelegramClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({DbConfiguration.class})
@Configuration
public class ScraperConfiguration {

    @Bean
    @ConfigurationProperties("db")
    public DbProperties dbProperties() {
        return new DbProperties();
    }

    @Bean
    @ConditionalOnClass({TlgLoginProperties.class, TlgClientProperties.class})
    public TelegramClient telegramClient(TlgLoginProperties loginProperties,
                                         TlgClientProperties clientProperties) {
        return new TlgConfiguration(loginProperties, clientProperties).getTelegram();
    }

    @Bean
    @ConfigurationProperties("telegram.login")
    @ConditionalOnProperty(prefix = "telegram.login", name = "phoneNumber")
    public TlgLoginProperties telegramLoginConfiguration() {
        return new TlgLoginProperties();
    }

    @Bean
    @ConfigurationProperties("telegram.client")
    @ConditionalOnProperty(prefix = "telegram.client", name = "apiId")
    public TlgClientProperties telegramClientConfiguration() {
        return new TlgClientProperties();
    }

}
