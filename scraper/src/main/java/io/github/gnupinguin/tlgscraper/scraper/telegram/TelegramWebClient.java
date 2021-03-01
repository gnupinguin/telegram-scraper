package io.github.gnupinguin.tlgscraper.scraper.telegram;

import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.web.ParsedEntity;

import javax.annotation.Nonnull;
import java.util.List;

public interface TelegramWebClient {

    ParsedEntity<Channel> searchChannel(@Nonnull String channel);

    List<ParsedEntity<WebMessage>>  getLastMessages(@Nonnull String channel, int count);

}
