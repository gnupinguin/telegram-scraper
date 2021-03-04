package io.github.gnupinguin.tlgscraper.scraper;

import io.github.gnupinguin.tlgscraper.db.DbConfiguration;
import io.github.gnupinguin.tlgscraper.db.orm.DbProperties;
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

}
