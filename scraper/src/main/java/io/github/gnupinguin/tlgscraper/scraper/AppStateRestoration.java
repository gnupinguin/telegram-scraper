package io.github.gnupinguin.tlgscraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.persistence.ApplicationStorage;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask.TaskStatus;
import io.github.gnupinguin.tlgscraper.scraper.persistence.repository.MentionTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties
@RequiredArgsConstructor
public class AppStateRestoration {

    private final MentionTaskRepository repository;
    private final ApplicationStorage storage;

    public static void main(String[] args) {
        SpringApplication.run(AppStateRestoration.class, args);
    }

    @PostConstruct
    public void restore() {
        List<MentionTask> locked = repository.getLocked(100);

        while (!locked.isEmpty()) {
            var lockedNames = locked.stream()
                    .map(MentionTask::getName)
                    .collect(Collectors.toList());
            log.info("Locked mentions: {}", locked);

            var restored = storage.restoreUnprocessedEntities(lockedNames);
            log.info("Chats were removed: {}", restored);
            lockedNames.removeAll(restored);

            repository.saveAll(restored.stream()
                    .map(m -> new MentionTask(m, TaskStatus.Initial))
                    .collect(Collectors.toList()));

            repository.saveAll(lockedNames.stream()
                    .map(m -> new MentionTask(m, TaskStatus.SuccessfullyProcessed))
                    .collect(Collectors.toList()));
            log.info("Chats were processed: {}", restored);
        }
    }

}
