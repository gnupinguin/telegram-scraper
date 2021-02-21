package org.textindexer.extractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.textindexer.extractor.scrapper.CrossChatScrapperImpl;

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
