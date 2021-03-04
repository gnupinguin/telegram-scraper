package io.github.gnupinguin.tlgscraper.scraper.telegram.parser;

import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface TelegramHtmlParser {

    @Nullable
    ParsedEntity<Channel> parseChannel(@Nonnull String html);

    @Nonnull
    List<ParsedEntity<WebMessage>> parseMessages(@Nonnull String html);

}
