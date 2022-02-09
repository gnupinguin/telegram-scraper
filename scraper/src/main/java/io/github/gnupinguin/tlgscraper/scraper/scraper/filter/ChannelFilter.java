package io.github.gnupinguin.tlgscraper.scraper.scraper.filter;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;

import javax.annotation.Nonnull;

public interface ChannelFilter {

    boolean doFilter(@Nonnull Channel chat);

}
