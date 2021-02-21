package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static io.github.gnupinguin.tlgscraper.db.repository.DbTools.questionMarks;

@Component
public class MessageRepositoryImpl extends AbstractRepository<Message> implements MessageRepository {

    private static final String INSERT_QUERY = "INSERT INTO message " +
            "(chat_id, " +
            "message_id, " +
            "reply_to_message_id, " +
            "forwarded_from_chat_id, " +
            "forwarded_from_message_id, " +
            "type, " +
            "text_content, " +
            "publish_date, " +
            "load_date, " +
            "views) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String DELETE_QUERY = "DELETE FROM message WHERE internal_id = ?;";
    private static final String DELETE_ALL = "DELETE FROM message;";

    public MessageRepositoryImpl(QueryExecutor queryExecutor, SqlEntityMapper<Message> mapper) {
        super(queryExecutor, mapper);
    }

    @Override
    public boolean save(Collection<Message> objects) {
        return saveWithIdGeneration(INSERT_QUERY, (id, e) -> e.setInternalId(id), objects);
    }

    @Override
    public @Nonnull List<Message> get(Collection<Long> ids) {
        String query = "SELECT internal_id, chat_id, message_id, reply_to_message_id, forwarded_from_chat_id, forwarded_from_message_id, type, text_content, publish_date, load_date, views FROM message WHERE internal_id IN " + questionMarks(ids.size()) + ";";
        return getInternal(query, ids);
    }

    @Override
    public boolean delete(Collection<Long> ids) {
        return deleteInternal(DELETE_QUERY, ids);
    }

    @Override
    public boolean delete() {
        return deleteInternal(DELETE_ALL);
    }

    @Override
    public @Nonnull List<Message> getForChat(Long chatId) {
        String query = "SELECT internal_id, chat_id, message_id, reply_to_message_id, forwarded_from_chat_id, forwarded_from_message_id, type, text_content, publish_date, load_date, views FROM message WHERE chat_id = ?;";
        return getInternal(query, List.of(chatId));
    }

}
