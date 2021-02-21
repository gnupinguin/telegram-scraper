package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractRepository<E>{

    protected final QueryExecutor queryExecutor;
    protected final SqlEntityMapper<E> mapper;

    public AbstractRepository(QueryExecutor queryExecutor, SqlEntityMapper<E> mapper) {
        this.queryExecutor = queryExecutor;
        this.mapper = mapper;
    }

    protected boolean saveWithIdGeneration(String query, BiConsumer<Long, E> idConsumer, Collection<E> objects) {
        var ids = queryExecutor.batchedUpdateQuery(query, mapper::toFields, objects, l -> (Long)l.get(0));
        if (!ids.isEmpty()) {
            int i = 0;
            for (E object : objects) {
                if (object != null) {
                    idConsumer.accept(ids.get(i++), object);
                }
            }
        }
        return true;
    }

    @Nonnull
    protected List<E> getInternal(String query, Collection<?> ids) {
        if (!ids.isEmpty()) {
            return queryExecutor.selectQuery(query, mapper::toObject, ids);
        }
        return List.of();
    }

    protected boolean deleteInternal(String query, Collection<?> ids) {
        if (!ids.isEmpty()) {
            queryExecutor.batchedUpdateQuery(query, List::of, ids, null);
        }
        return true;
    }

    protected boolean deleteInternal(String query) {
        queryExecutor.batchedUpdateQuery(query, List::of, List.of(), null);
        return true;
    }

}
