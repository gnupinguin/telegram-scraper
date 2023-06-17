package io.github.gnupinguin.tlgscraper.scraper.scraper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record ScraperConfiguration(int messagesCount, int maxFailures) {

}
