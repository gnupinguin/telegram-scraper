package io.github.gnupinguin.tlgscraper.scraper.persistence.repository;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Mention;
import org.springframework.data.repository.CrudRepository;

public interface MentionRepository extends CrudRepository<Mention, Long> {
}
