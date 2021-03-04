package io.github.gnupinguin.tlgscraper.scraper.scrapper;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.db.Mention;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import io.github.gnupinguin.tlgscraper.scraper.persistence.ApplicationStorage;
import io.github.gnupinguin.tlgscraper.scraper.persistence.MentionQueue;
import io.github.gnupinguin.tlgscraper.scraper.scrapper.filter.ChatFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrossChatScrapperImplTest {

    private static final String CHANNEL_NAME = "channel";

    public static final String MENTION1 = "channel1";
    public static final String MENTION2 = "channel2";

    @Mock
    private ChatScrapper chatScrapper;

    @Mock
    private MentionQueue mentionQueue;

    @Mock
    private ApplicationStorage storage;

    @Mock
    private ChatFilter filter;

    @InjectMocks
    private CrossChatScrapperImpl crossChatScrapper;

    private Chat chat;

    @Before
    public void setUp() {
        chat = Chat.builder().name(CHANNEL_NAME).build();
        List<Message> messages= List.of(message(MENTION1), message(MENTION2));
        chat.setMessages(messages);

        when(chatScrapper.scrap(CHANNEL_NAME, 300))
                .thenReturn(chat);

        when(mentionQueue.poll())
                .thenReturn(CHANNEL_NAME, new String[]{null});

        when(filter.doFilter(chat))
                .thenReturn(true);
    }

    @Test
    public void testSuccessfulDeepScrap() {
        crossChatScrapper.deepScrap(List.of(CHANNEL_NAME));
        verify(mentionQueue, times(1)).add(List.of(CHANNEL_NAME));

        verify(storage, times(1)).save(chat);

        verify(mentionQueue, times(1)).add(List.of(MENTION1, MENTION2));
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testPlainScrap() {
        crossChatScrapper.plainScrap(List.of(CHANNEL_NAME));
        verify(mentionQueue, never()).add(List.of(CHANNEL_NAME));
        verify(mentionQueue, never()).poll();
        verify(storage, times(1)).save(chat);
        verify(mentionQueue, times(1)).add(List.of(MENTION1, MENTION2));
    }

    @Test
    public void testChatNotFoundScrap() {
        when(chatScrapper.scrap(CHANNEL_NAME, 300))
                .thenReturn(null);
        crossChatScrapper.deepScrap(List.of(CHANNEL_NAME));
        verify(mentionQueue, times(1)).add(List.of(CHANNEL_NAME));
        verify(storage, never()).save(any());
        verify(mentionQueue, times(1)).markInvalid(CHANNEL_NAME);
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testStopAfter20Failures() {
        when(mentionQueue.poll())
                .thenReturn(CHANNEL_NAME);
        when(chatScrapper.scrap(CHANNEL_NAME, 300))
                .thenReturn(null);
        crossChatScrapper.deepScrap(List.of());
        verify(storage, never()).save(any());
        verify(mentionQueue, times(20)).markInvalid(CHANNEL_NAME);
        verify(mentionQueue, times(21)).poll();
        verify(mentionQueue, times(1)).restore(Collections.nCopies(20, CHANNEL_NAME));
    }

    @Test
    public void testFilterBotNameFromQueue() {
        when(mentionQueue.poll())
                .thenReturn("someBoT", new String[]{null});

        crossChatScrapper.deepScrap(List.of());
        verify(storage, never()).save(any());
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testFilterBotNameFromMessage() {
        List<Message> messages = List.of(message(MENTION1), message("Bot"));
        chat.setMessages(messages);
        crossChatScrapper.deepScrap(List.of());

        verify(storage, times(1)).save(chat);
        verify(mentionQueue, times(1)).add(List.of(MENTION1));
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testChatFilter() {
        when(filter.doFilter(chat))
                .thenReturn(false);
        crossChatScrapper.deepScrap(List.of());
        verify(storage, never()).save(any());
        verify(mentionQueue, times(1)).markFiltered(CHANNEL_NAME);
        verify(mentionQueue, times(2)).poll();
    }

    @Nonnull
    private Message message(String mention) {
        return Message.builder()
                .channel(chat)
                .mentions(Set.of(Mention.builder().chatName(mention).build()))
                .build();
    }

}