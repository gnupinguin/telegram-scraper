package io.github.gnupinguin.tlgscraper.scraper.proxy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfusingHeaders {

    private final String userAgent;

    private final String uniqueUserAgent;

    private final String accept;

}
