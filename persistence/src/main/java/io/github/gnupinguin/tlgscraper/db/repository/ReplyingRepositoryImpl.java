package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Replying;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Component
public class ReplyingRepositoryImpl extends AbstractRepository<Replying> implements ReplyingRepository {

    private static final String DELETE_ALL = "DELETE FROM replying;";
    private static final String INSERT_QUERY = "INSERT INTO replying (internal_message_id, reply_to_message_id) VALUES (?, ?);";
    private static final String DELETE_QUERY = "DELETE FROM replying WHERE internal_message_id = ?;";

    public ReplyingRepositoryImpl(QueryExecutor queryExecutor, SqlEntityMapper<Replying> mapper) {
        super(queryExecutor, mapper);
    }

    @Override
    public boolean save(Collection<Replying> objects) {
        return saveWithoutIdGeneration(INSERT_QUERY, objects);
    }

    @Nonnull
    @Override
    public List<Replying> get(Collection<Long> ids) {
        String query = "SELECT internal_message_id, reply_to_message_id FROM replying WHERE internal_message_id IN " + DbTools.questionMarks(ids.size()) + ";";
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
