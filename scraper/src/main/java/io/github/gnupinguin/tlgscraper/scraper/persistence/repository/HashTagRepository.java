package io.github.gnupinguin.tlgscraper.scraper.persistence.repository;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.HashTag;
import org.springframework.data.repository.CrudRepository;

public interface HashTagRepository extends CrudRepository<HashTag, Long> {
}
