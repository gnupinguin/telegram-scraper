package io.github.gnupinguin.tlgscraper.scraper.persistence;


import io.github.gnupinguin.tlgscraper.model.db.Chat;

import javax.annotation.Nonnull;
import java.util.List;

public interface ApplicationStorage {

    void save(@Nonnull Chat chat);

    List<String> restore(List<String> blockedMentions);

}
