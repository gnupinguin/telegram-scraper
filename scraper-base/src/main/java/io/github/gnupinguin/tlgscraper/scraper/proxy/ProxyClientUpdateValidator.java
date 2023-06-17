package io.github.gnupinguin.tlgscraper.scraper.proxy;

import java.util.Optional;
import java.util.function.Function;

public interface ProxyClientUpdateValidator {

    boolean validate(Function<String, Optional<String>> urlResponse);

}
