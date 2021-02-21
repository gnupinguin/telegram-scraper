package org.textindexer.extractor.messages;

import io.github.gnupinguin.tlgscraper.tlgservice.telegram.TelegramClientImpl;
import org.drinkless.tdlib.TdApi;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MessagesServiceImplTest {

    private final long CHAT_ID = 1;

    private final TelegramClientImpl telegram = mock(TelegramClientImpl.class);
    private final MessagesService messagesService = new MessagesServiceImpl(telegram);

    @Test
    public void checkOldMessagesWhenMessagesSeparatedOnTwoBatches() {
        final TdApi.Message[] messages1 = generateMessages(3, 5);
        final TdApi.Messages chatHistory1 = new TdApi.Messages(2, messages1);
        final TdApi.Message[] messages2 = generateMessages(1, 2);
        final TdApi.Messages chatHistory2 = new TdApi.Messages(3, messages2);

        Mockito.when(telegram.getChatHistory(CHAT_ID, 5, 0, 5))
                .thenReturn(chatHistory1);
        Mockito.when(telegram.getChatHistory(CHAT_ID, 3, 0, 2))
                .thenReturn(chatHistory2);

        final List<TdApi.Message> messages = messagesService.fetchOld(CHAT_ID, 5, 5);
        assertEquals(5, messages.size());
        assertTrue(messages.containsAll(asList(messages1)));
        assertTrue(messages.containsAll(asList(messages2)));
    }

    @Test
    public void checkOldMessagesWhenTotalCountIsLessThenRequested() {
        final TdApi.Message[] messages1 = generateMessages(4, 5);
        final TdApi.Messages chatHistory = new TdApi.Messages(2, messages1);

        Mockito.when(telegram.getChatHistory(CHAT_ID, 5, 0, 5))
                .thenReturn(chatHistory);
        Mockito.when(telegram.getChatHistory(CHAT_ID, 4, 0, 3))
                .thenReturn(new TdApi.Messages(0, null) );

        final List<TdApi.Message> messages = messagesService.fetchOld(CHAT_ID, 5, 5);
        assertEquals(2, messages.size());
        assertTrue(messages.containsAll(asList(messages1)));
    }

    @Test
    public void checkFreshMessagesWhenMessagesSeparatedOnTwoBatches() {
        final TdApi.Message[] messages1 = generateMessages(1, 2);
        final TdApi.Messages chatHistory1 = new TdApi.Messages(2, messages1);
        final TdApi.Message[] messages2 = generateMessages(3, 5);
        final TdApi.Messages chatHistory2 = new TdApi.Messages(3, messages2);

        Mockito.when(telegram.getChatHistory(CHAT_ID, 1, -5, 5))
                .thenReturn(chatHistory1);
        Mockito.when(telegram.getChatHistory(CHAT_ID, 2, -3, 3))
                .thenReturn(chatHistory2);

        final List<TdApi.Message> messages = messagesService.fetchFresh(CHAT_ID, 1, 5);
        assertEquals(5, messages.size());
        assertTrue(messages.containsAll(asList(messages1)));
        assertTrue(messages.containsAll(asList(messages2)));
    }

    private TdApi.Message getMessage(long id) {
        final TdApi.Message message = new TdApi.Message();
        message.id = id;
        return message;
    }

    private TdApi.Message[] generateMessages(long fromIndex, long toIndex) {
        return LongStream.rangeClosed(fromIndex, toIndex)
                .boxed()
                .sorted(Collections.reverseOrder())
                .map(this::getMessage)
                .toArray(TdApi.Message[]::new);
    }

}