package io.github.gnupinguin.tlgscraper.db.queue;

import java.util.Collection;
import java.util.List;

public interface StatusTaskQueue<T extends StatusTask> {

    boolean updateStatuses(Collection<T> tasks);

    List<MentionTask> poll(int count);

    List<MentionTask> getLocked(int count);

    boolean add(Collection<T> tasks);

}
