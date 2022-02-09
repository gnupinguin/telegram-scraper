package io.github.gnupinguin.tlgscraper.scraper.scraper.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class WebChannel {

    private final String name;

    private final String title;

    private final String description;

    private final int users;

}
