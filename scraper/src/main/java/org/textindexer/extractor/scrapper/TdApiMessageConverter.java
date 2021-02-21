package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;
import org.drinkless.tdlib.TdApi;

import javax.annotation.Nonnull;

public interface TdApiMessageConverter {

    @Nonnull
    MessageInfo convert(@Nonnull TdApi.Message message);

}
