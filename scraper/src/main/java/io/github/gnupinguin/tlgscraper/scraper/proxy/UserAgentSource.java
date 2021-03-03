package io.github.gnupinguin.tlgscraper.scraper.proxy;

public interface UserAgentSource {

    String nextUserAgent();

    String nextUniqueAgent();

}
