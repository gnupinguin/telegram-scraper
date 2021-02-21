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

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NativeClientChannelSearcherTest {

    private static final long CHAT_ID = -100123456789L;
    private static final int SUPERGROUP_ID = 123456789;
    private static final long NOT_SUPERGROUP_CHAT_ID = -1L;
    private static final String CHANNEL_NAME = "channel";
    private static final String DESCRIPTION = "Channel description";

    @Mock
    private TelegramClient telegram;

    @InjectMocks
    private NativeClientChannelSearcher searcher;

    @BeforeClass
    public static void init() {
        System.loadLibrary("tdjni");
    }

    @Before
    public void setUp() {

        when(telegram.searchPublicChats(CHANNEL_NAME))
                .thenReturn(new TdApi.Chats(1,new long[]{CHAT_ID}));

        when(telegram.searchFollowedChats(CHANNEL_NAME, 5))
                .thenReturn(new TdApi.Chats(1,new long[]{CHAT_ID}));

        when(telegram.getChat(CHAT_ID))
                .thenReturn(getChat(true));

        when(telegram.getSupergroup(SUPERGROUP_ID))
                .thenReturn(getSupergroup(CHANNEL_NAME));
    }

    @Test
    public void testChannelFound() {
        Optional<Chat> channel = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(channel.isPresent());
        assertEquals(new Chat(CHAT_ID, CHANNEL_NAME, DESCRIPTION, 1), channel.get());
    }

    @Test
    public void testScrapIgnoreNotMatchesChatNames() {
        TdApi.Supergroup supergroup = getSupergroup(CHANNEL_NAME.toUpperCase());
        when(telegram.getSupergroup(SUPERGROUP_ID))
                .thenReturn(supergroup);
        Optional<Chat> channel = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(channel.isEmpty());
        verify(telegram, times(1)).searchFollowedChats(CHANNEL_NAME, 5);
    }

    @Test
    public void testScrapIgnoreNotChannelSupergroup() {
        when(telegram.getChat(CHAT_ID))
                .thenReturn(getChat(false));
        Optional<Chat> channel = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(channel.isEmpty());
        verify(telegram, times(1)).searchFollowedChats(CHANNEL_NAME, 5);
        verify(telegram, never()).getSupergroup(SUPERGROUP_ID);
    }

    @Test
    public void testScrapIgnoreNotSupergroupChat() {
        TdApi.Chat chat = getChat(true);
        chat.type = new TdApi.ChatTypeBasicGroup();
        when(telegram.getChat(CHAT_ID))
                .thenReturn(chat);

        Optional<Chat> channel = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(channel.isEmpty());
        verify(telegram, times(1)).searchFollowedChats(CHANNEL_NAME, 5);
        verify(telegram, never()).getSupergroup(SUPERGROUP_ID);
    }

    @Test
    public void testScrapIgnoreNotSupergroupId() {
        when(telegram.searchPublicChats(CHANNEL_NAME))
                .thenReturn(new TdApi.Chats(1, new long[]{NOT_SUPERGROUP_CHAT_ID}));
        when(telegram.searchFollowedChats(CHANNEL_NAME, 5))
                .thenReturn(new TdApi.Chats(1,new long[]{NOT_SUPERGROUP_CHAT_ID}));

        Optional<Chat> channel = searcher.searchChannel(CHANNEL_NAME);
        assertTrue(channel.isEmpty());
        verify(telegram, times(1)).searchFollowedChats(CHANNEL_NAME, 5);
        verify(telegram, never()).getSupergroup(SUPERGROUP_ID);
        verify(telegram, never()).getChat(NOT_SUPERGROUP_CHAT_ID);
    }

    @Nonnull
    private TdApi.Supergroup getSupergroup(String name) {
        final TdApi.Supergroup supergroup = new TdApi.Supergroup();
        supergroup.id = NativeClientChannelSearcherTest.SUPERGROUP_ID;
        supergroup.username = name;
        supergroup.memberCount = 1;
        supergroup.isChannel = true;
        return supergroup;
    }

    @Nonnull
    private TdApi.Chat getChat(boolean isChannel) {
        final TdApi.Chat chat = new TdApi.Chat();
        chat.id = NativeClientChannelSearcherTest.CHAT_ID;
        chat.type = new TdApi.ChatTypeSupergroup(SUPERGROUP_ID, isChannel);
        chat.title = DESCRIPTION;
        return chat;
    }

}