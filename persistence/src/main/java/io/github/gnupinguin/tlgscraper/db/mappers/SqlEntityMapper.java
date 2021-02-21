package io.github.gnupinguin.tlgscraper.db.mappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface SqlEntityMapper<T> {

    @Nonnull
    List<Object> toFields(@Nonnull T entity);

    @Nullable
    T toObject(@Nonnull List<Object> fields);

}
