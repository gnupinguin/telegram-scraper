package io.github.gnupinguin.tlgscraper.scraper.scrapper.filter;

import io.github.gnupinguin.tlgscraper.model.db.Chat;

import javax.annotation.Nonnull;

public interface ChatFilter {

    boolean doFilter(@Nonnull Chat chat);

}
