package org.textindexer.extractor.persistence;


import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;

import javax.annotation.Nonnull;
import java.util.List;

public interface ApplicationStorage {

    void save(@Nonnull Chat channel, @Nonnull List<MessageInfo> messages);

    List<String> restore(List<String> blockedMentions);

}
