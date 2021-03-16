package io.github.gnupinguin.tlgscraper.scraper;

import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import io.github.gnupinguin.tlgscraper.db.queue.mention.MentionTask;
import io.github.gnupinguin.tlgscraper.db.queue.mention.MentionTaskQueueImpl;
import io.github.gnupinguin.tlgscraper.scraper.persistence.ApplicationStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.stream.Collectors;

@Slf4j
public class AppStateRestoration {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        var mentionTaskQueue = context.getBean(MentionTaskQueueImpl.class);
        var storage = context.getBean(ApplicationStorage.class);

        var locked = mentionTaskQueue.getLocked(100).stream()
                .map(MentionTask::getName)
                .collect(Collectors.toList());
        log.info("Locked mentions: {}", locked);

        var restored = storage.restore(locked);
        log.info("Chats were removed: {}", restored);
        locked.removeAll(restored);

        mentionTaskQueue.update(restored.stream()
                .map(m -> new MentionTask(TaskStatus.Initial, m))
                .collect(Collectors.toList()));

        mentionTaskQueue.update(locked.stream()
                .map(m -> new MentionTask(TaskStatus.SuccessfullyProcessed, m))
                .collect(Collectors.toList()));
        log.info("Chats were processed: {}", restored);
    }

}
