package io.github.gnupinguin.tlgscraper.scraper.telegram;

import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

public interface TelegramWebClient {

    @Nullable
    ParsedEntity<WebChannel> searchChannel(@Nonnull String channel);

    @Nonnull
    List<ParsedEntity<WebMessage>> getLastMessages(@Nonnull String channel, int count);

    @Nonnull
    List<ParsedEntity<WebMessage>> getMessagesBefore(@Nonnull String channel, long beforeId, int count);

}
