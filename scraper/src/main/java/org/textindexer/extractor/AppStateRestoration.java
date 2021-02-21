package org.textindexer.extractor;

import io.github.gnupinguin.tlgscraper.db.queue.MentionTask;
import io.github.gnupinguin.tlgscraper.db.queue.MentionTaskQueue;
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.textindexer.extractor.persistence.ApplicationStorage;

import java.util.stream.Collectors;

@Slf4j
public class AppStateRestoration {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        var mentionTaskQueue = context.getBean(MentionTaskQueue.class);
        var storage = context.getBean(ApplicationStorage.class);

        var locked = mentionTaskQueue.getLocked(100).stream()
                .map(MentionTask::getName)
                .collect(Collectors.toList());
        log.info("Locked mentions: {}", locked);

        var restored = storage.restore(locked);
        log.info("Chats were removed: {}", restored);
        locked.removeAll(restored);

        mentionTaskQueue.updateStatuses(restored.stream()
                .map(m -> new MentionTask(TaskStatus.Initial, m))
                .collect(Collectors.toList()));

        mentionTaskQueue.updateStatuses(locked.stream()
                .map(m -> new MentionTask(TaskStatus.SuccessfullyProcessed, m))
                .collect(Collectors.toList()));
        log.info("Chats were processed: {}", restored);
    }

}
