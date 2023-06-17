package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.Channel;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;


public interface ChannelScrapper {

    @Nullable
    Channel scrap(@Nonnull String channel, int count);

    void scrap(@Nonnull Channel channel, long beforeMessageId, int count);

}
