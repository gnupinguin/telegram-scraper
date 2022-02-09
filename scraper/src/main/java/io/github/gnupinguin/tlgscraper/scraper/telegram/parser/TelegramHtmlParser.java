package io.github.gnupinguin.tlgscraper.scraper.telegram.parser;

import io.github.gnupinguin.tlgscraper.scraper.scraper.model.WebChannel;
import io.github.gnupinguin.tlgscraper.scraper.scraper.model.WebMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface TelegramHtmlParser {

    @Nullable
    ParsedEntity<WebChannel> parseChannel(@Nonnull String html);

    @Nonnull
    List<ParsedEntity<WebMessage>> parseMessages(@Nonnull String html);

}
