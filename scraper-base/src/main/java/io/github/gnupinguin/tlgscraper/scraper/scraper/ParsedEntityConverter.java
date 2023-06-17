package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import jakarta.annotation.Nonnull;

import java.util.List;

public interface ParsedEntityConverter {

    @Nonnull
    Channel convert(@Nonnull ParsedEntity<WebChannel> parsedChannel,
                    @Nonnull List<ParsedEntity<WebMessage>> parsedMessages);

    void update(@Nonnull Channel channel,
                @Nonnull List<ParsedEntity<WebMessage>> parsedMessages);

}
