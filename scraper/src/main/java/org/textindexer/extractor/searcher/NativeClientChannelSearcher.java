package org.textindexer.extractor.searcher;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.tlgservice.telegram.TelegramClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "scraper", name = "strategy", havingValue = "Native")
@RequiredArgsConstructor
public class NativeClientChannelSearcher implements ChannelSearcher {

    private static final Pattern SUPERGROUP_PATTERN = Pattern.compile("^-100[1-9]+");
    private static final int LIMIT_CHANNELS_SEARCH = 5;

    private final TelegramClient telegram;

    @Override
    public Optional<Chat> searchChannel(@Nonnull String name) {
        log.info("Searching channel '{}'", name);
        TdApi.Chats chats = telegram.searchPublicChats(name);
        return searchTelegramChannel(name, chats).or(() -> {
            var followedChats = telegram.searchFollowedChats(name, LIMIT_CHANNELS_SEARCH);
            return searchTelegramChannel(name, followedChats);
        });
    }

    @Nonnull
    private Optional<Chat> searchTelegramChannel(String channelName, TdApi.Chats chats) {
        log.info("Chats found for channel '{}' analyzing: {}", channelName, chats.chatIds.length);
        return Arrays.stream(chats.chatIds)
                .limit(LIMIT_CHANNELS_SEARCH)
                .mapToObj(chat -> getSupergroupChannel(channelName, chat))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<Chat> getSupergroupChannel(String channelName, long chatId) {
        if (isSupergroupId(chatId)) {
            TdApi.Chat chat = telegram.getChat(chatId);
            TdApi.ChatType type = chat.type;
            if (type instanceof TdApi.ChatTypeSupergroup) {
                TdApi.ChatTypeSupergroup supergroupType = (TdApi.ChatTypeSupergroup) type;
                int supergroupId = ((TdApi.ChatTypeSupergroup) type).supergroupId;
                if (supergroupType.isChannel) {
                    TdApi.Supergroup supergroup = telegram.getSupergroup(supergroupId);
                    if (supergroup.username.equals(channelName)) {
                        log.info("Channel '{}' found", channelName);
                        return Optional.of(new Chat(chatId, supergroup.username, chat.title, supergroup.memberCount));
                    }
                } else {
                    log.info("'{}' is not channel", channelName);
                }
            } else {
                log.info("'{}' is not supergroup", channelName);
            }
        } else {
            log.info("'{}' has not supergroup id", channelName);
        }
        return Optional.empty();
    }

    private boolean isSupergroupId(long id) {
        return SUPERGROUP_PATTERN.matcher(String.valueOf(id)).matches();
    }

}
