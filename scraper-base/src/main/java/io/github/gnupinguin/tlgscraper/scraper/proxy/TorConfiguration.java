package io.github.gnupinguin.tlgscraper.scraper.proxy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


public record TorConfiguration(String host, int socksPort, int httpPort, int controlPort, String password) {

}
