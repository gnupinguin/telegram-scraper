package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Mention;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Component
public class MentionRepositoryImpl extends AbstractRepository<Mention> implements MentionRepository {

    private static final String INSERT_QUERY = "INSERT INTO mention (internal_message_id, chat_name) VALUES (?, ?);";
    private static final String DELETE_ALL = "DELETE FROM mention;";
    private static final String DELETE_QUERY = "DELETE FROM mention WHERE id = ?;";

    public MentionRepositoryImpl(QueryExecutor queryExecutor, SqlEntityMapper<Mention> mapper) {
        super(queryExecutor, mapper);
    }

    @Override
    public boolean save(Collection<Mention> objects) {
        return saveWithIdGeneration(INSERT_QUERY, (id, e) -> e.setId(id), objects);
    }

    @Override
    public @Nonnull List<Mention> get(Collection<Long> ids) {
        String query = "SELECT id, internal_message_id, chat_name FROM mention WHERE id IN " + DbTools.questionMarks(ids.size()) + ";";
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

}
