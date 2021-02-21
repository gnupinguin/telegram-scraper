package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.textindexer.extractor.persistence.ApplicationStorage;
import org.textindexer.extractor.searcher.ChannelSearcher;
import org.textindexer.extractor.searcher.LanguageDetector;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrossChatScrapperImplTest {

    private static final long CHAT_ID = -1001L;
    private static final String CHANNEL_NAME = "channel";

    public static final String DESCRIPTION = "Channel description";
    public static final String MENTION1 = "channel1";
    public static final String MENTION2 = "channel2";
    @Mock
    private ChannelSearcher channelSearcher;

    @Mock
    private ChatScrapper chatScrapper;

    @Mock
    private MentionQueue mentionQueue;

    @Mock
    private ApplicationStorage storage;

    @Mock
    private LanguageDetector languageDetector;

    @InjectMocks
    private  CrossChatScrapperImpl crossChatScrapper;

    private List<MessageInfo> foundMessages;
    private Chat chat;

    @Before
    public void setUp() {
        foundMessages = List.of(message(List.of(MENTION1)), message(List.of(MENTION2)));
        chat = new Chat(CHAT_ID, CHANNEL_NAME, DESCRIPTION, 1);

        when(chatScrapper.scrap(CHAT_ID, 300))
                .thenReturn(foundMessages);

        when(mentionQueue.poll())
                .thenReturn(CHANNEL_NAME, new String[]{null});

        when(channelSearcher.searchChannel(CHANNEL_NAME))
                .thenReturn(Optional.of(chat));

        when(languageDetector.detectLanguage(anyList()))
                .thenReturn(true);
    }

    @Test
    public void testSuccessfulDeepScrap() {
        crossChatScrapper.deepScrap(List.of(CHANNEL_NAME));
        verify(mentionQueue, times(1)).add(List.of(CHANNEL_NAME));
        verify(storage, times(1)).save(chat, foundMessages);
        verify(mentionQueue, times(1)).add(List.of(MENTION1, MENTION2));
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testPlainScrap() {
        crossChatScrapper.plainScrap(List.of(CHANNEL_NAME));
        verify(mentionQueue, never()).add(List.of(CHANNEL_NAME));
        verify(mentionQueue, never()).poll();
        verify(storage, times(1)).save(chat, foundMessages);
        verify(mentionQueue, times(1)).add(List.of(MENTION1, MENTION2));
    }

    @Test
    public void testChatNotFoundScrap() {
        when(channelSearcher.searchChannel(CHANNEL_NAME))
                .thenReturn(Optional.empty());
        crossChatScrapper.deepScrap(List.of(CHANNEL_NAME));
        verify(mentionQueue, times(1)).add(List.of(CHANNEL_NAME));
        verify(storage, never()).save(any(), anyList());
        verify(mentionQueue, times(1)).markInvalid(CHANNEL_NAME);
        verify(mentionQueue, times(2)).poll();
        verifyNoInteractions(chatScrapper);

    }

    @Nonnull
    private MessageInfo message(List<String> mentions) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMentions(mentions);
        messageInfo.setLinks(List.of());
        return messageInfo;
    }

}