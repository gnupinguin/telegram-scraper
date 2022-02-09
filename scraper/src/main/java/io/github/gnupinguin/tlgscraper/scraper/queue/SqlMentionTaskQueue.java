package io.github.gnupinguin.tlgscraper.scraper.queue;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask;
import io.github.gnupinguin.tlgscraper.scraper.persistence.repository.MentionTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SqlMentionTaskQueue implements MentionTaskQueue {

    private final MentionTaskRepository repository;

    @Override
    public void update(List<MentionTask> tasks) {
        repository.saveAll(tasks);
    }

    @Override
    public List<MentionTask> poll(int count) {
        List<MentionTask> tasks = repository.getNext(count);
        log.info("Fetched tasks count: {}", tasks.size());
        return tasks;
    }

    @Override
    public void restore(List<MentionTask> tasks) {

    }

}
