package io.github.gnupinguin.tlgscraper.db.queue;

public interface Task<ID, E> {

    ID getId();

    E getEntity();

    TaskStatus getStatus();

    void setStatus(TaskStatus status);

}
