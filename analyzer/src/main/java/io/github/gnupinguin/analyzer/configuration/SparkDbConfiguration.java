package io.github.gnupinguin.analyzer.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.io.Serializable;
import java.util.Properties;

@Getter
@ToString
@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties("spark.database")
public class SparkDbConfiguration implements Serializable {

    private final String jdbcUrl;

    private final Properties props;

}
