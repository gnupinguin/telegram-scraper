package io.github.gnupinguin.tlgscraper.model.scraper.web;

import lombok.Data;

@Data
public class Channel {

    private final String name;

    private final String title;

    private final String description;

    private final int users;

}
