package io.github.gnupinguin.tlgscraper.scraper.telegram;

import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface TelegramWebClient {

    @Nullable
    ParsedEntity<Channel> searchChannel(@Nonnull String channel);

    @Nonnull
    List<ParsedEntity<WebMessage>> getLastMessages(@Nonnull String channel, int count);

}
