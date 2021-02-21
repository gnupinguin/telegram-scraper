package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.model.db.Chat;

import java.util.List;

public interface ChatRepository extends Repository<Long, Chat> {

    List<Chat> getChatsByNames(List<String> names);
}
