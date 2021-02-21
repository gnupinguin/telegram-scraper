package io.github.gnupinguin.tlgscraper.tlgservice.telegram;

import io.github.gnupinguin.tlgscraper.tlgservice.handlers.BaseAwaitHandler;
import io.github.gnupinguin.tlgscraper.tlgservice.handlers.awaitService.HandlersAwaitService;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import javax.annotation.Nonnull;

import static io.github.gnupinguin.tlgscraper.tlgservice.handlers.HandlerFactory.defaultHandler;

public class TelegramClientImpl implements TelegramClient {

    private static final HandlersAwaitService HANDLERS_AWAIT_SERVICE = new HandlersAwaitService();

    private final Client client;

    public TelegramClientImpl(Client client) {
        this.client = client;
    }

    @Override
    public TdApi.Chat searchPublicChat(@Nonnull String channelName) {
        final BaseAwaitHandler<TdApi.Chat> handler = defaultHandler("getChatHandler");
        client.send(new TdApi.SearchPublicChat(channelName), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

    @Override
    public TdApi.Chats searchPublicChats(@Nonnull String request) {
        final BaseAwaitHandler<TdApi.Chats> handler = defaultHandler("SearchChatsHandler");
        client.send(new TdApi.SearchPublicChats(request), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

    @Override
    public TdApi.Chat getChat(long id) {
        final BaseAwaitHandler<TdApi.Chat> handler = defaultHandler("GetChatHandler");
        client.send(new TdApi.GetChat(id), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

    @Override
    public TdApi.Messages getChatHistory(long chatId, long startId, int offset, int limit) {
        final BaseAwaitHandler<TdApi.Messages> handler = defaultHandler("GetChatHistoryHandler");
        client.send(new TdApi.GetChatHistory(chatId, startId, offset, limit, false), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

    @Override
    public TdApi.Message getMessage(long chatId, long messageId) {
        final BaseAwaitHandler<TdApi.Message> handler = defaultHandler("GetMessageHandler");
        client.send(new TdApi.GetMessage(chatId, messageId), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

    @Override
    public TdApi.Supergroup getSupergroup(int id) {
        final BaseAwaitHandler<TdApi.Supergroup> handler = defaultHandler("GetSuperGroupHandler");
        client.send(new TdApi.GetSupergroup(id), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

    @Override
    public TdApi.MessageLinkInfo getMessageLinkInfo(@Nonnull String link) {
        final BaseAwaitHandler<TdApi.MessageLinkInfo> handler = defaultHandler("GetMessageLinkInfoHandler");
        client.send(new TdApi.GetMessageLinkInfo(link), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

    @Override
    public TdApi.Chat joinChatByInviteLink(@Nonnull String link) {
        final BaseAwaitHandler<TdApi.Chat> handler = defaultHandler("JoinChatByInviteLinkHandler");
        client.send(new TdApi.JoinChatByInviteLink(link), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

    @Override
    public TdApi.Chats searchFollowedChats(@Nonnull String query, int limit) {
        final BaseAwaitHandler<TdApi.Chats> handler = defaultHandler("SearchFollowedChats");
        client.send(new TdApi.SearchChats(query, limit), handler);
        return HANDLERS_AWAIT_SERVICE.get(handler);
    }

}
