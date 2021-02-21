package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.HashTag;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Component
public class HashTagRepositoryImpl extends AbstractRepository<HashTag> implements HashTagRepository {

    private static final String INSERT_QUERY = "INSERT INTO hashtag (internal_message_id, tag) VALUES (?, ?);";
    private static final String DELETE_QUERY = "DELETE FROM hashtag WHERE id = ?;";
    private static final String DELETE_ALL = "DELETE FROM hashtag;";

    public HashTagRepositoryImpl(QueryExecutor queryExecutor, SqlEntityMapper<HashTag> mapper) {
        super(queryExecutor, mapper);
    }

    @Override
    public boolean save(Collection<HashTag> objects) {
        return saveWithIdGeneration(INSERT_QUERY, (id, e) -> e.setId(id), objects);
    }

    @Override
    public @Nonnull List<HashTag> get(Collection<Long> ids) {
        String query = "SELECT id, internal_message_id, tag FROM hashtag WHERE id IN " + DbTools.questionMarks(ids.size()) + ";";
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
