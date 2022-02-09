package io.github.gnupinguin.tlgscraper.scraper.queue;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask;

import java.util.List;

public interface MentionTaskQueue {

    List<MentionTask> poll(int count);

    void update(List<MentionTask> tasks);

    void restore(List<MentionTask> tasks);

}
