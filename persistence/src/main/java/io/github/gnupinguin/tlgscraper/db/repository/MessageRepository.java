package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.model.db.Message;

import javax.annotation.Nonnull;
import java.util.List;

public interface MessageRepository extends Repository<Long, Message> {

    @Nonnull
    List<Message> getForChat(Long chatId);

}
