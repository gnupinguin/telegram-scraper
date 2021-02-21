package io.github.gnupinguin.tlgscraper.tlgservice.telegram;

import org.drinkless.tdlib.TdApi;

import javax.annotation.Nonnull;

public interface TelegramClient {

    TdApi.Chat searchPublicChat(@Nonnull String channelName);

    TdApi.Chats searchPublicChats(@Nonnull String request);

    TdApi.Chats searchFollowedChats(@Nonnull String query, int limit);

    TdApi.Chat getChat(long id);

    TdApi.Messages getChatHistory(long chatId, long startId, int offset, int limit);

    TdApi.Message getMessage(long chatId, long messageId);

    TdApi.Supergroup getSupergroup(int id);

    TdApi.MessageLinkInfo getMessageLinkInfo(@Nonnull String link);

    TdApi.Chat joinChatByInviteLink(@Nonnull String link);

}
