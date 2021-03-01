package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Forwarding;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Component
public class ForwardingRepositoryImpl extends AbstractRepository<Forwarding> implements ForwardingRepository {

    private static final String DELETE_ALL = "DELETE FROM forwarding;";
    private static final String INSERT_QUERY = "INSERT INTO forwarding (internal_message_id, forwarded_from_channel, forwarded_from_message_id) VALUES (?, ?, ?);";
    private static final String DELETE_QUERY = "DELETE FROM forwarding WHERE internal_message_id = ?;";

    public ForwardingRepositoryImpl(QueryExecutor queryExecutor, SqlEntityMapper<Forwarding> mapper) {
        super(queryExecutor, mapper);
    }

    @Override
    public boolean save(Collection<Forwarding> objects) {
        return saveWithoutIdGeneration(INSERT_QUERY, objects);
    }

    @Nonnull
    @Override
    public List<Forwarding> get(Collection<Long> ids) {
        String query = "SELECT internal_message_id, forwarded_from_channel, forwarded_from_message_id FROM forwarding WHERE internal_message_id IN " + DbTools.questionMarks(ids.size()) + ";";
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
