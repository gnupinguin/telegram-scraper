package org.textindexer.extractor.messages;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.drinkless.tdlib.TdApi;

import java.util.List;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class MessageSequence {

    private final long chatId;

    private final long startId;

    @With
    private final int batchLimit;

    private final List<TdApi.Message> messages;

}
