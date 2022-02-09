package io.github.gnupinguin.tlgscraper.scraper.persistence;


import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;

import javax.annotation.Nonnull;
import java.util.List;

public interface ApplicationStorage {

    void save(@Nonnull Channel chat);

    List<String> restoreUnprocessedEntities(List<String> blockedMentions);

}
