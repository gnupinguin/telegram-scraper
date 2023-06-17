package io.github.gnupinguin.tlgscraper.scraper.telegram.parser;

import lombok.Data;

import java.util.Date;
import java.util.Set;

public record ParsedEntity<T>(T entity, Date loadDate, Set<String> mentions, Set<String> links, Set<String> hashTags) {

}
