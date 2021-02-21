package org.textindexer.extractor.searcher;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.tlgservice.telegram.TelegramClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.textindexer.extractor.telegram.TelegramWebClient;

import javax.annotation.Nonnull;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;

@Slf4j
@Primary
@Component
@ConditionalOnProperty(prefix = "scraper", name = "strategy", havingValue = "Web")
@RequiredArgsConstructor
public class WebApiChannelSearcher implements ChannelSearcher {

    private final TelegramWebClient webClient;
    private final TelegramClient telegram;

    @Override
    public Optional<Chat> searchChannel(@Nonnull String name) {
        try {
            if (webClient.channelPresent(name)) {
                TdApi.Chat chat = telegram.searchPublicChat(name);
                TdApi.ChatType type = chat.type;
                if (type instanceof TdApi.ChatTypeSupergroup) {
                    TdApi.ChatTypeSupergroup supergroupType = (TdApi.ChatTypeSupergroup) type;
                    if (supergroupType.isChannel) {
                        int supergroupId = supergroupType.supergroupId;
                        TdApi.Supergroup supergroup = telegram.getSupergroup(supergroupId);
                        return Optional.of(new Chat(chat.id, supergroup.username, chat.title, supergroup.memberCount));
                    } else {
                        log.info("'{}' is not channel", name);
                    }
                } else {
                    log.info("'{}' is not supergroup", name);
                }
            } else {
                log.info("Channel '{}' do not present in web", name);
            }
        } catch (Exception e) {
            if (e instanceof UndeclaredThrowableException) {
                log.info("Native telegram request failed for '{}'", name, e);
                throw new RuntimeException(e);
            } else {
                log.error("Request failed for '{}' channel", name, e);
            }
        }
        return Optional.empty();
    }

}
