package io.github.gnupinguin.tlgscraper.scraper.notification;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask;

import java.util.Collection;

public interface Notificator {

    void send(String message);

    boolean waitApprove(String message);

    boolean approveRestoration(Collection<MentionTask> channels);

    void sendException(Exception e);

}
