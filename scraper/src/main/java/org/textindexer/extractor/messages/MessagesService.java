package org.textindexer.extractor.messages;

import org.drinkless.tdlib.TdApi;

import javax.annotation.Nonnull;
import java.util.List;

public interface MessagesService {

    @Nonnull
    List<TdApi.Message> fetchFresh(long chatId, long startId, int count);

    @Nonnull
    List<TdApi.Message> fetchOld(long chatId, long startId, int count);

    @Nonnull
    TdApi.Message get(long chatId, long messageId);

}
