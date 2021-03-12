package io.github.gnupinguin.tlgscraper.scraper.notification;

import java.util.Collection;

public interface Notificator {

    void send(String message);

    boolean waitApprove(String message);

    boolean approveRestoration(Collection<String> channels);

    void sendException(Exception e);

}
