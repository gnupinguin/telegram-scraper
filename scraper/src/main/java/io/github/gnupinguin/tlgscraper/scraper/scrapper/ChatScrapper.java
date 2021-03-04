package io.github.gnupinguin.tlgscraper.scraper.scrapper;

import io.github.gnupinguin.tlgscraper.model.db.Chat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ChatScrapper {

    @Nullable
    Chat scrap(@Nonnull String channel, int count);

}
