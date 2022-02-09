package io.github.gnupinguin.tlgscraper.scraper.persistence.repository;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Message;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {

    List<Message> getMessagesByChannel(@Nonnull Channel channel);

}
