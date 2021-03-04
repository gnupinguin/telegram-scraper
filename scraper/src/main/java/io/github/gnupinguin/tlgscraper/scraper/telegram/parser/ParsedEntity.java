package io.github.gnupinguin.tlgscraper.scraper.telegram.parser;

import lombok.Data;

import java.util.Set;

@Data
public class ParsedEntity<T> {

    private final T entity;

    private final Set<String> mentions;

    private final Set<String> links;

    private final Set<String> hashTags;

}
