package io.github.gnupinguin.tlgscraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.utils.Profiles;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class LocalRun {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", Profiles.LOCAL);
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    }

}
