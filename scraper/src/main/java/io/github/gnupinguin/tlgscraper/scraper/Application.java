package io.github.gnupinguin.tlgscraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.runner.MultiThreadScraper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        var bean = context.getBean(MultiThreadScraper.class);
        bean.scrap();
    }

}
