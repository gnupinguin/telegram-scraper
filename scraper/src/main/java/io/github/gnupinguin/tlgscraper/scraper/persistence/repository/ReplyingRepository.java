package io.github.gnupinguin.tlgscraper.scraper.persistence.repository;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Replying;
import org.springframework.data.repository.CrudRepository;

public interface ReplyingRepository extends CrudRepository<Replying, Long> {
}
