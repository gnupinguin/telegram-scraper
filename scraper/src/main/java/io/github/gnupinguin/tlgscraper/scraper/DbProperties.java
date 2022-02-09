package io.github.gnupinguin.tlgscraper.scraper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConfigurationProperties("db")
@ConstructorBinding
@RequiredArgsConstructor
public class DbProperties {

    private final String url;

    private final String username;

    private final String password;

}
