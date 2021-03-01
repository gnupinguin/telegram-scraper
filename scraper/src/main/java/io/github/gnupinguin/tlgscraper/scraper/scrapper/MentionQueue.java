package io.github.gnupinguin.tlgscraper.scraper.scrapper;

import java.util.List;

public interface MentionQueue {

    String poll();

    void add(List<String> mentions);

    void markInvalid(String mention);

    void markUndefined(String mention);

    void restore(List<String> failedMentions);
}
