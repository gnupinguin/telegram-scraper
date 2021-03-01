package io.github.gnupinguin.tlgscraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.scrapper.CrossChatScrapperImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        var bean = context.getBean(CrossChatScrapperImpl.class);
        bean.deepScrap(List.of());
    }

}
