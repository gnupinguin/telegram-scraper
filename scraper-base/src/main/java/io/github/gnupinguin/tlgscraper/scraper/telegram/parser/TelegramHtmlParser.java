package io.github.gnupinguin.tlgscraper.scraper.telegram.parser;

import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

public interface TelegramHtmlParser {

    @Nullable
    ParsedEntity<WebChannel> parseChannel(@Nonnull String html);

    @Nonnull
    List<ParsedEntity<WebMessage>> parseMessages(@Nonnull String html);

}
