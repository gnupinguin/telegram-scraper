package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ChannelScrapper {

    @Nullable
    Channel scrap(@Nonnull String channel, int count);

    void scrap(@Nonnull Channel channel, long beforeMessageId, int count);

}
