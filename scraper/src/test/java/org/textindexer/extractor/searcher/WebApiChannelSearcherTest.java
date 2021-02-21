package org.textindexer.extractor.searcher;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.tlgservice.telegram.TelegramClient;
import org.drinkless.tdlib.TdApi;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.textindexer.extractor.telegram.TelegramWebClient;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebApiChannelSearcherTest {

    private static final long CHAT_ID = -100123456789L;
    private static final int SUPERGROUP_ID = 123456789;
    private static final String CHANNEL_NAME = "channel";
    private static final String DESCRIPTION = "Channel description";

    @Mock
    private TelegramWebClient client;

    @Mock
    private TelegramClient telegram;

    @InjectMocks
    private WebApiChannelSearcher searcher;

    @BeforeClass
    public static void init() {
        System.loadLibrary("tdjni");
    }

    @Before
    public void setUp() throws Exception {
        when(client.channelPresent(CHANNEL_NAME))
                .thenReturn(true);

        when(telegram.searchPublicChat(CHANNEL_NAME))
                .thenReturn(getChat(true));

        when(telegram.getSupergroup(SUPERGROUP_ID))
                .thenReturn(getSupergroup(CHANNEL_NAME));
    }

    @Test
    public void testSuccessfulChannelSearch() {
        Optional<Chat> chat = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(chat.isPresent());
        assertEquals(new Chat(CHAT_ID, CHANNEL_NAME, DESCRIPTION, 1), chat.get());
    }

    @Test
    public void testScrapIgnoreNotChannelSupergroup() {
        when(telegram.searchPublicChat(CHANNEL_NAME))
                .thenReturn(getChat(false));
        Optional<Chat> channel = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(channel.isEmpty());
        verify(telegram, never()).getSupergroup(SUPERGROUP_ID);
    }

    @Test
    public void testScrapIgnoreNotSupergroupChat() {
        TdApi.Chat chat = getChat(true);
        chat.type = new TdApi.ChatTypeBasicGroup();
        when(telegram.searchPublicChat(CHANNEL_NAME))
                .thenReturn(chat);

        Optional<Chat> channel = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(channel.isEmpty());
        verify(telegram, never()).getSupergroup(SUPERGROUP_ID);
    }

    @Test
    public void testScrapFailedForNotChannelHttpResponse() {
        when(client.channelPresent(CHANNEL_NAME))
                .thenReturn(false);
        Optional<Chat> channel = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(channel.isEmpty());
        verify(telegram, never()).searchPublicChat(CHANNEL_NAME);
        verify(telegram, never()).getSupergroup(SUPERGROUP_ID);
    }

    private TdApi.Supergroup getSupergroup(String name) {
        final TdApi.Supergroup supergroup = new TdApi.Supergroup();
        supergroup.id = SUPERGROUP_ID;
        supergroup.username = name;
        supergroup.memberCount = 1;
        supergroup.isChannel = true;
        return supergroup;
    }

    private TdApi.Chat getChat(boolean isChannel) {
        final TdApi.Chat chat = new TdApi.Chat();
        chat.id = CHAT_ID;
        chat.type = new TdApi.ChatTypeSupergroup(SUPERGROUP_ID, isChannel);
        chat.title = DESCRIPTION;
        return chat;
    }

}