package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;

import javax.annotation.Nonnull;
import java.util.List;

public interface ParsedEntityConverter {

    @Nonnull
    Chat convert(@Nonnull ParsedEntity<Channel> parsedChannel,
                 @Nonnull List<ParsedEntity<WebMessage>> parsedMessages);

    void update(@Nonnull Chat chat,
                @Nonnull List<ParsedEntity<WebMessage>> parsedMessages);

}
