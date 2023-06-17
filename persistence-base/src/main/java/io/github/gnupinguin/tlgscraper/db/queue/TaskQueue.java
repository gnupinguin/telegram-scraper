package io.github.gnupinguin.tlgscraper.db.queue;

import java.util.Collection;
import java.util.List;

public interface TaskQueue<T extends Task<?, ?>> {

    boolean update(Collection<T> tasks);

    List<T> poll(int count);

    List<T> getLocked(int count);

    boolean add(Collection<T> tasks);

}
