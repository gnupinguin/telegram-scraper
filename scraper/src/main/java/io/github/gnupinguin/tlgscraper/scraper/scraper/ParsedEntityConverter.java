package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import io.github.gnupinguin.tlgscraper.scraper.scraper.model.WebChannel;
import io.github.gnupinguin.tlgscraper.scraper.scraper.model.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;

import javax.annotation.Nonnull;
import java.util.List;

public interface ParsedEntityConverter {

    @Nonnull
    Channel convert(@Nonnull ParsedEntity<WebChannel> parsedChannel,
                    @Nonnull List<ParsedEntity<WebMessage>> parsedMessages);

    void update(@Nonnull Channel chat,
                @Nonnull List<ParsedEntity<WebMessage>> parsedMessages);

}
