package io.github.gnupinguin.tlgscraper.db.repository;


import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Link;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static io.github.gnupinguin.tlgscraper.db.repository.DbTools.questionMarks;


@Component
public class LinkRepositoryImpl extends AbstractRepository<Link> implements LinkRepository {

    private static final String INSERT_QUERY = "INSERT INTO link (internal_message_id, url) VALUES (?, ?);";
    private static final String DELETE_ALL = "DELETE FROM link;";
    private static final String DELETE_QUERY = "DELETE FROM link WHERE id = ?;";

    public LinkRepositoryImpl(QueryExecutor queryExecutor, SqlEntityMapper<Link> mapper) {
        super(queryExecutor, mapper);
    }

    @Override
    public boolean save(Collection<Link> objects) {
        return saveWithIdGeneration(INSERT_QUERY, (id, e) -> e.setId(id), objects);
    }

    @Override
    public @Nonnull List<Link> get(Collection<Long> ids) {
        String query = "SELECT id, internal_message_id, url FROM link WHERE id IN " + questionMarks(ids.size()) + ";";
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
