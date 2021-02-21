package org.textindexer.extractor;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.textindexer.extractor.utils.ScraperProfiles;

public class LocalRun {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", ScraperProfiles.LOCAL);
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    }

}
