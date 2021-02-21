package org.textindexer.extractor.scrapper;

import java.util.List;

public interface MentionQueue {

    String poll();

    void add(List<String> mentions);

    void markInvalid(String mention);

    void markUndefined(String mention);

    void restore(List<String> failedMentions);
}
