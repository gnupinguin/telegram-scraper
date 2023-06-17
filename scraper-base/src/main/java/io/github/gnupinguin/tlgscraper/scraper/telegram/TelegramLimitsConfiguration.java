package io.github.gnupinguin.tlgscraper.scraper.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record TelegramLimitsConfiguration(long minTimeRangeMs, long maxTimeRangeMs) {

}
