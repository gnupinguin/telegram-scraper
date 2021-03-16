package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.Chat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ChatScrapper {

    @Nullable
    Chat scrap(@Nonnull String channel, int count);

    void scrap(@Nonnull Chat channel, long beforeMessageId, int count);

}
