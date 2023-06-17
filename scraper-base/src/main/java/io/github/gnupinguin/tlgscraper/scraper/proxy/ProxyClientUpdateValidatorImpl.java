package io.github.gnupinguin.tlgscraper.scraper.proxy;

import java.util.Optional;
import java.util.function.Function;

public class ProxyClientUpdateValidatorImpl implements ProxyClientUpdateValidator {

    private static final String CONFIRMED_CHANNEL = "http://t.me/nexta_live";

    @Override
    public boolean validate(Function<String, Optional<String>> urlResponse) {
        return urlResponse.apply(CONFIRMED_CHANNEL)
                .map(response -> response.contains("Preview channel"))
                .orElse(false);
    }

}
