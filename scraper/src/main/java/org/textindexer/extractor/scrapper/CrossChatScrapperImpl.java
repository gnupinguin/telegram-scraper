package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.textindexer.extractor.persistence.ApplicationStorage;
import org.textindexer.extractor.searcher.ChannelSearcher;
import org.textindexer.extractor.searcher.LanguageDetector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrossChatScrapperImpl implements CrossChatScrapper {

    private static final int DEFAULT_MESSAGES_COUNT = 300;
    private static final int MAX_FAILED_CHANNELS_COUNT = 20;

    private final ChatScrapper chatScrapper;
    private final ChannelSearcher channelSearcher;
    private final MentionQueue mentionQueue;
    private final ApplicationStorage storage;
    private final LanguageDetector detector;

    private final List<String> failedMentions = Collections.synchronizedList(new ArrayList<>(MAX_FAILED_CHANNELS_COUNT));

    @Override
    public void deepScrap(@Nonnull List<String> chatNames) {
        mentionQueue.add(chatNames);

        Stream.generate(mentionQueue::poll)
                .takeWhile(this::canContinue)
                .map(this::searchChannel)
                .forEach(channel -> channel.ifPresent(this::scrap));
        log.info("Scrapping finished");
    }

    private boolean canContinue(@Nullable String name) {
        if (failedMentions.size() >= MAX_FAILED_CHANNELS_COUNT) {
            mentionQueue.restore(failedMentions);
            log.info("Many chats were not found, scrapping will be stopped");
            log.info("Restored mentions: {}", failedMentions);
            return false;
        }
        return name != null;
    }

    @Override
    public void plainScrap(@Nonnull List<String> chatNames) {
        chatNames.stream()
                .map(this::searchChannel)
                .forEach(channel -> channel.ifPresent(this::scrap));
        log.info("Scrapping finished");
    }

    private Optional<Chat> searchChannel(@Nonnull String name) {
        if (isBotName(name)) {
            log.info("Channel '{}' has name like a bot", name);
            return Optional.empty();
        }
        return channelSearcher.searchChannel(name).or(() -> {
            failedMentions.add(name);
            mentionQueue.markInvalid(name);
            return Optional.empty();
        });
    }

    private void scrap(Chat channel) {
        failedMentions.clear();
        log.info("Start scrapping for '{}'", channel);
        var scrappedMessages = chatScrapper.scrap(channel.getId(), DEFAULT_MESSAGES_COUNT);
        if (detector.detectLanguage(extractText(scrappedMessages))) {
            storage.save(channel, scrappedMessages);
            mentionQueue.add(extractMentions(scrappedMessages));
        } else {
            log.info("Can not detect channel language: {}", channel);
            mentionQueue.markUndefined(channel.getName());
        }
    }

    @Nonnull
    private List<String> extractText(List<MessageInfo> scrappedMessages) {
        return scrappedMessages.stream()
                .map(MessageInfo::getTextContent)
                .collect(Collectors.toList());
    }

    private boolean isBotName(@Nonnull String name) {
        return name.toLowerCase().endsWith("bot");
    }

    @Nonnull
    private List<String> extractMentions(List<MessageInfo> scrappedMessages) {
        return scrappedMessages.stream()
                .map(MessageInfo::getMentions)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

}
