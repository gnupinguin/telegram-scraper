package io.github.gnupinguin.tlgscraper.scraper.telegram;

import io.github.gnupinguin.tlgscraper.scraper.scraper.model.WebChannel;
import io.github.gnupinguin.tlgscraper.scraper.scraper.model.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface TelegramWebClient {

    @Nullable
    ParsedEntity<WebChannel> searchChannel(@Nonnull String channel);

    @Nonnull
    List<ParsedEntity<WebMessage>> getLastMessages(@Nonnull String channel, int count);

    @Nonnull
    List<ParsedEntity<WebMessage>> getMessagesBefore(@Nonnull String channel, long beforeId, int count);

}
