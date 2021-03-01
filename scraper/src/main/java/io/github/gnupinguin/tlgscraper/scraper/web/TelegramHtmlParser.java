package io.github.gnupinguin.tlgscraper.scraper.web;

import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;

import javax.annotation.Nonnull;
import java.util.List;

public interface TelegramHtmlParser {

    ParsedEntity<Channel> parseChannel(@Nonnull String html);

    List<ParsedEntity<WebMessage>> parseMessages(@Nonnull String html);

}
