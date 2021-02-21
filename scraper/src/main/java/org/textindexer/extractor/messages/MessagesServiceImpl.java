package org.textindexer.extractor.messages;

import io.github.gnupinguin.tlgscraper.tlgservice.telegram.TelegramClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.Collections.emptyList;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagesServiceImpl implements MessagesService {

    private static final int MAX_OLD_MESSAGES_BATCH_SIZE = 100;
    private static final int MAX_FRESH_MESSAGES_BATCH_SIZE = 99;

    private final TelegramClient telegram;

    @Nonnull
    @Override
    public List<TdApi.Message> fetchFresh(long chatId, long startId, int count) {
        return fetch(chatId, startId, count, this::nextFresh);
    }

    @Nonnull
    @Override
    public List<TdApi.Message> fetchOld(long chatId, long startId, int count) {
        return fetch(chatId, startId, count, this::nextOld);
    }

    @Nonnull
    @Override
    public TdApi.Message get(long chatId, long messageId) {
        return telegram.getMessage(chatId, messageId);
    }

    @Nonnull
    private MessageSequence nextOld(@Nonnull MessageSequence sequence) {
        final TdApi.Messages chatHistory = telegram.getChatHistory(sequence.getChatId(), sequence.getStartId(), 0, sequence.getBatchLimit());// -1 for include start message
        return next(chatHistory, sequence, this::getLastMessageId);
    }

    @Nonnull
    private MessageSequence nextFresh(@Nonnull MessageSequence sequence) {
        final TdApi.Messages chatHistory = telegram.getChatHistory(sequence.getChatId(), sequence.getStartId(), getFreshOffset(sequence), sequence.getBatchLimit());
        return next(chatHistory, sequence, this::getFirstMessageId);
    }

    private int getFreshOffset(@Nonnull MessageSequence sequence) {
        return -Math.min(sequence.getBatchLimit(), MAX_FRESH_MESSAGES_BATCH_SIZE);
    }

    @Nonnull
    private List<TdApi.Message> fetch(long chatId, long startId, int count,
                                    UnaryOperator<MessageSequence> iterator) {
        List<TdApi.Message> messages = new ArrayList<>(count);
        int batchLimit = calculateBatchLimit(count);
        MessageSequence sequence = initSequence(chatId, startId, batchLimit);
        while (count > 0) {
            sequence = iterator.apply(sequence);
            if (!isFinishedSequence(sequence)) {
                messages.addAll(sequence.getMessages());
                count -= sequence.getMessages().size();
                batchLimit = calculateBatchLimit(count);
                sequence = sequence.withBatchLimit(batchLimit);
            } else{
                return messages;
            }
        }
        return messages;
    }

    @Nonnull
    private MessageSequence initSequence(long chatId, long startId, int batchLimit) {
        return MessageSequence.builder()
                .chatId(chatId)
                .startId(startId)
                .batchLimit(batchLimit)
                .build();
    }

    private int calculateBatchLimit(int count) {
        return Math.min(MAX_OLD_MESSAGES_BATCH_SIZE, count);
    }

    private boolean isFinishedSequence(@Nonnull MessageSequence sequence) {
        return sequence.getMessages().isEmpty();
    }

    @Nonnull
    private MessageSequence next(@Nonnull TdApi.Messages chatHistory,
                                 @Nonnull MessageSequence sequence,
                                 @Nonnull Function<TdApi.Messages, Long> nextIdExtractor) {
        if (isFinishedSequence(chatHistory)) {
            return sequence.toBuilder()
                    .messages(emptyList())
                    .build();
        }
        return sequence.toBuilder()
                .messages(Arrays.asList(chatHistory.messages))
                .startId(nextIdExtractor.apply(chatHistory))
                .build();
    }

    private long getLastMessageId(TdApi.Messages chatHistory) {
        return chatHistory.messages[chatHistory.messages.length - 1].id;
    }

    private long getFirstMessageId(TdApi.Messages chatHistory) {
        return chatHistory.messages[0].id;
    }

    private boolean isFinishedSequence(TdApi.Messages chatHistory) {
        return chatHistory.totalCount == 0;
    }

}
