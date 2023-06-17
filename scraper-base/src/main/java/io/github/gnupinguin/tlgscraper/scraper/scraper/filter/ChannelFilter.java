package io.github.gnupinguin.tlgscraper.scraper.scraper.filter;

import io.github.gnupinguin.tlgscraper.model.db.Channel;
import jakarta.annotation.Nonnull;

public interface ChannelFilter {

    boolean doFilter(@Nonnull Channel channel);

}
