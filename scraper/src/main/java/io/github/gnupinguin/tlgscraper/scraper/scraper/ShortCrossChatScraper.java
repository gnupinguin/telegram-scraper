package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import io.github.gnupinguin.tlgscraper.db.queue.mention.BufferedMentionTaskQueue;
import io.github.gnupinguin.tlgscraper.db.queue.mention.MentionTask;
import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.db.Mention;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import io.github.gnupinguin.tlgscraper.scraper.notification.Notificator;
import io.github.gnupinguin.tlgscraper.scraper.persistence.ApplicationStorage;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShortCrossChatScraper implements CrossChatScraper {

    private final ChatScrapper chatScrapper;
    private final BufferedMentionTaskQueue mentionTaskQueue;
    private final ApplicationStorage storage;
    private final ChatFilter filter;
    private final Notificator notificator;
    private final ScraperConfiguration configuration;

    private final List<MentionTask> failedMentions = Collections.synchronizedList(new ArrayList<>(100));
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicBoolean scrapingEnabled = new AtomicBoolean(true);

    @Override
    public void scrap() {
        Stream.generate(mentionTaskQueue::poll)
                .takeWhile(this::canContinue)
                .filter(not(this::isBotName))
                .forEach(this::scrap);
        log.info("Scrapping finished");
    }

    private boolean canContinue(@Nullable MentionTask task) {
        if (task != null) {
            lock.readLock().lock();
            try {
                if (failedMentions.size() < configuration.getMaxFailures()) {
                    return true;
                }
            } finally {
                lock.readLock().unlock();
            }

            lock.writeLock().lock();
            try {
                return sendNotification(task);
            } finally {
                lock.writeLock().unlock();
            }
        }
        return false;
    }

    private boolean sendNotification(@Nonnull MentionTask task) {
        if (scrapingEnabled.get()) {
            log.info("Many chats were not found");
            if (notificator.approveRestoration(failedMentions)) {
                log.info("Approving for channel restoration got");
                failedMentions.add(task);
                mentionTaskQueue.restore(failedMentions);
                log.info("Restored mentions: {}", failedMentions);
                scrapingEnabled.set(false);
                return false;
            } else {
                log.info("Channels restoration was discarded");
                failedMentions.clear();
                return true;
            }
        } else {
            mentionTaskQueue.restore(List.of(task));
            return false;
        }
    }

    private void scrap(@Nonnull MentionTask task) {
        String channel = task.getName();
        log.info("Start scrapping for '{}'", channel);
        var chat = chatScrapper.scrap(channel, configuration.getMessagesCount());
        if (chat != null) {
            failedMentions.clear();
            if (filter.doFilter(chat)) {
                storage.save(chat);
                mentionTaskQueue.add(extractMentions(chat));
            } else {
                log.info("Can not detect channel language: {}", channel);
                mentionTaskQueue.markFiltered(task);
            }
        } else {
            log.info("Channel '{}' not found", channel);
            failedMentions.add(task);
            mentionTaskQueue.markInvalid(task);
        }
    }

    private boolean isBotName(@Nonnull MentionTask task) {
        return task.getName()
                .toLowerCase().endsWith("bot");
    }

    @Nonnull
    private List<MentionTask> extractMentions(Chat chat) {
        return chat.getMessages().stream()
                .map(Message::getMentions)
                .flatMap(Collection::stream)
                .map(Mention::getChatName)
                .distinct()
                .map(name -> new MentionTask(TaskStatus.Initial, name))
                .filter(not(this::isBotName))
                .collect(Collectors.toList());
    }

}
