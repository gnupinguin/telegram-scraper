package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;

import javax.annotation.Nonnull;
import java.util.List;

public interface ChatScrapper {

    @Nonnull
    List<MessageInfo> scrap(long chatId, int count);

    @Nonnull
    List<MessageInfo> scrap(long chatId, long messageId, int count);

}
