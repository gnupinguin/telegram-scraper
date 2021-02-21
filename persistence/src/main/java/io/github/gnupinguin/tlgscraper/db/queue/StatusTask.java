package io.github.gnupinguin.tlgscraper.db.queue;

public interface StatusTask {

    TaskStatus getStatus();

    void setStatus(TaskStatus status);

}
