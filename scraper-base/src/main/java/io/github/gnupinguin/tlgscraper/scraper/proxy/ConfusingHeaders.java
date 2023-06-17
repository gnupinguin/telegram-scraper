package io.github.gnupinguin.tlgscraper.scraper.proxy;

import lombok.Builder;
import lombok.Data;


public record ConfusingHeaders(String userAgent, String uniqueUserAgent, String accept) {

}
