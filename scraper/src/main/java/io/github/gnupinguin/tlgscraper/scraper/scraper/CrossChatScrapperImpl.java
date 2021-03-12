package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.db.Mention;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import io.github.gnupinguin.tlgscraper.scraper.notification.Notificator;
import io.github.gnupinguin.tlgscraper.scraper.persistence.ApplicationStorage;
import io.github.gnupinguin.tlgscraper.scraper.persistence.MentionQueue;
import io.github.gnupinguin.tlgscraper.scraper.scraper.filter.ChatFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrossChatScrapperImpl implements CrossChatScrapper {

    private final ChatScrapper chatScrapper;
    private final MentionQueue mentionQueue;
    private final ApplicationStorage storage;
    private final ChatFilter filter;
    private final Notificator notificator;
    private final ScraperConfiguration configuration;

    private final List<String> failedMentions = Collections.synchronizedList(new ArrayList<>(100));

    @Override
    public void scrapFromQueue() {
        Stream.generate(mentionQueue::poll)
                .takeWhile(this::canContinue)
                .forEach(this::scrap);
        log.info("Scrapping finished");
    }

    private boolean canContinue(@Nullable String name) {
        if (failedMentions.size() >= configuration.getMaxFailures()) {
            log.info("Many chats were not found");
            if (notificator.approveRestoration(failedMentions)) {
                log.info("Approving for channel restoration got");
                mentionQueue.restore(failedMentions);
                log.info("Restored mentions: {}", failedMentions);
                return false;
            } else {
                log.info("Channels restoration was discarded");
                failedMentions.clear();
            }
        }
        return name != null;
    }

    @Override
    public void plainScrap(@Nonnull List<String> chatNames) {
        chatNames.forEach(this::scrap);
        log.info("Scrapping finished");
    }

    private void scrap(@Nonnull String channel) {
        if (isBotName(channel)) {
            log.info("Channel '{}' has name like a bot", channel);
            return;
        }
        log.info("Start scrapping for '{}'", channel);
        var chat = chatScrapper.scrap(channel, configuration.getMessagesCount());
        if (chat != null) {
            failedMentions.clear();
            if (filter.doFilter(chat)) {
                storage.save(chat);
                mentionQueue.add(extractMentions(chat));
            } else {
                log.info("Can not detect channel language: {}", channel);
                mentionQueue.markFiltered(channel);
            }
        } else {
            log.info("Channel '{}' not found", channel);
            failedMentions.add(channel);
            mentionQueue.markInvalid(channel);
        }
    }

    private boolean isBotName(@Nonnull String name) {
        return name.toLowerCase().endsWith("bot");
    }

    @Nonnull
    private List<String> extractMentions(Chat chat) {
        return chat.getMessages().stream()
                .map(Message::getMentions)
                .flatMap(Collection::stream)
                .map(Mention::getChatName)
                .filter(not(this::isBotName))
                .distinct()
                .collect(Collectors.toList());
    }

}
