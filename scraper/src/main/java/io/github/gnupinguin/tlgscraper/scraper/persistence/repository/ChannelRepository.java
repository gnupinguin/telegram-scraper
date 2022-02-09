package io.github.gnupinguin.tlgscraper.scraper.persistence.repository;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import java.util.List;

public interface ChannelRepository extends CrudRepository<Channel, Long> {

    Channel getChannelByName(@Nonnull String name);

    List<Channel> getChannelsByNameIsIn(@Nonnull List<String> names);

}
