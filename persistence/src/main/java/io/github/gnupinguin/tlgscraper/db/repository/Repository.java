package io.github.gnupinguin.tlgscraper.db.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface Repository<ID, E> {

    boolean save(Collection<E> objects);

    default boolean save(@Nonnull E object) {
        return save(List.of(object));
    }

    @Nullable
    default E get(ID id) {
        List<E> objects = get(List.of(id));
        if (!objects.isEmpty()) {
            return objects.get(0);
        }
        return null;
    }

    @Nonnull
    List<E> get(Collection<ID> ids);

    boolean delete(Collection<ID> ids);

    boolean delete();

}