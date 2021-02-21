package io.github.gnupinguin.tlgscraper.db.repository;


import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Chat;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;


@Component
public class ChatRepositoryImpl extends AbstractRepository<Chat> implements ChatRepository {

    private static final String DELETE_ALL = "DELETE FROM chat;";
    private static final String INSERT_QUERY = "INSERT INTO chat (id, name, description, members) VALUES (?, ?, ?, ?);";
    private static final String DELETE_QUERY = "DELETE FROM chat WHERE id = ?;";

    public ChatRepositoryImpl(QueryExecutor queryExecutor, SqlEntityMapper<Chat> mapper) {
        super(queryExecutor, mapper);
    }

    @Override
    public boolean save(Collection<Chat> objects) {
        queryExecutor.batchedUpdateQuery(INSERT_QUERY, mapper::toFields, objects, null);
        return true;
    }

    @Nonnull
    @Override
    public List<Chat> get(Collection<Long> ids) {
        String query = "SELECT id, name, description, members FROM chat WHERE id IN " + DbTools.questionMarks(ids.size()) + ";";
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
    public List<Chat> getChatsByNames(List<String> names) {
        String query = "SELECT id, name, description, members FROM chat WHERE name IN " + DbTools.questionMarks(names.size()) + ";";
        return getInternal(query, names);
    }

}
