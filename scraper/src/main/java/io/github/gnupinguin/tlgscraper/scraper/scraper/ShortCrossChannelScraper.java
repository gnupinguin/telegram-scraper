package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.notification.Notificator;
import io.github.gnupinguin.tlgscraper.scraper.persistence.ApplicationStorage;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Mention;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask.TaskStatus;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Message;
import io.github.gnupinguin.tlgscraper.scraper.queue.MentionTaskQueue;
import io.github.gnupinguin.tlgscraper.scraper.scraper.filter.ChannelFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
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
public class ShortCrossChannelScraper implements CrossChannelScraper {

    private final ChannelScrapper channelScrapper;
    private final MentionTaskQueue mentionTaskQueue;
    private final ApplicationStorage storage;
    //TODO support multifiltering for channel
    private final ChannelFilter filter;
    private final Notificator notificator;
    private final ScraperConfiguration configuration;

    private final List<MentionTask> failedMentions = Collections.synchronizedList(new ArrayList<>(100));
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicBoolean scrapingEnabled = new AtomicBoolean(true);

    @Override
    public void scrap() {
        var tasks = mentionTaskQueue.poll(configuration.getFetchingMentionTasksCount());
        while (canContinue(tasks)) {
            var processed = tasks.stream()
                    .flatMap(this::scrap)
                    .collect(Collectors.toList());
            mentionTaskQueue.update(processed);
            tasks = mentionTaskQueue.poll(configuration.getFetchingMentionTasksCount());
        }
        log.info("Scrapping finished");
    }

    private boolean canContinue(@Nonnull List<MentionTask> tasks) {
        while (!tasks.isEmpty()) {
            lock.readLock().lock();
            try {
                if (failedMentions.size() < configuration.getMaxFailures()) {
                    return true;
                }
            } finally {
                lock.readLock().unlock();
            }
            if (lock.writeLock().tryLock()) {
                //TODO fix synchronization
                try {
                    return sendNotification(tasks);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }
        return false;
    }

    private Stream<MentionTask> scrap(@Nonnull MentionTask task) {
        String channelName = task.getName();
        log.info("Start scrapping for '{}'", channelName);
        var channel = channelScrapper.scrap(channelName, configuration.getChannelMessagesCount());
        if (channel != null) {
            failedMentions.clear();
            if (filter.doFilter(channel)) {
                storage.save(channel);
                var mentionTasks = extractMentions(channel);
                return Stream.concat(mentionTasks, Stream.of(task.withStatus(TaskStatus.SuccessfullyProcessed)));
            } else {
                log.info("Can not detect channel language: {}", channel);
                return Stream.of(task.withStatus(TaskStatus.Filtered));
            }
        } else {
            log.info("Channel '{}' not found", channelName);
            failedMentions.add(task);
            return Stream.of(task.withStatus(TaskStatus.InvalidProcessed));
        }
    }

    private boolean isBotName(@Nonnull MentionTask task) {
        return task.getName()
                .toLowerCase().endsWith("bot");
    }

    @Nonnull
    private Stream<MentionTask> extractMentions(Channel channel) {
        return channel.getMessages().stream()
                .map(Message::getMentions)
                .flatMap(Collection::stream)
                .map(Mention::getChannelName)
                .distinct()
                .map(name -> new MentionTask(name, TaskStatus.Initial))
                .filter(not(this::isBotName));
    }

    private boolean sendNotification(@Nonnull List<MentionTask> tasks) {
        if (scrapingEnabled.get()) {
            log.info("Many chats were not found");
            if (notificator.approveRestoration(failedMentions)) {
                log.info("Approving for channel restoration got");
                failedMentions.addAll(tasks);
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
            mentionTaskQueue.restore(tasks);
            return false;
        }
    }

}
