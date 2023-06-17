package io.github.gnupinguin.tlgscraper.db.queue;

import java.util.List;

public interface BufferedTaskQueue<T extends Task<?, ?>> {

    T poll();

    void add(List<T> tasks);

    void markInvalid(T task);

    void markUndefined(T task);

    void markFiltered(T task);

    void restore(List<T> tasks);

}
