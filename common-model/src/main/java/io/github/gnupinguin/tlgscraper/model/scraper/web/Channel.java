package io.github.gnupinguin.tlgscraper.model.scraper.web;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class Channel {

    private final String name;

    private final String title;

    private final String description;

    private final int users;

}
