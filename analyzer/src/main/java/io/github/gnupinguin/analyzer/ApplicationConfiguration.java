package io.github.gnupinguin.analyzer;

import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public SparkSession localSparkSession() {
        return SparkSession.builder()
                .appName("Local application")
                .master("local")
                .getOrCreate();
    }


}
