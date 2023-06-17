package io.github.gnupinguin.tlgscraper.scraper.persistence;


import io.github.gnupinguin.tlgscraper.model.db.Channel;
import jakarta.annotation.Nonnull;

import java.util.List;

public interface ApplicationStorage {

    void save(@Nonnull Channel channel);

}
