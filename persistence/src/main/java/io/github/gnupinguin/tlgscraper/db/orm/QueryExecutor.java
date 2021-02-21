package io.github.gnupinguin.tlgscraper.db.orm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface QueryExecutor {

    <T, R> List<R> batchedUpdateQuery(@Nonnull String query,
                                      @Nonnull Function<T, List<Object>> objectSerializer,
                                      @Nonnull Collection<T> objects,
                                      @Nullable Function<List<Object>, R> objectDeserializer);

    @Nonnull <T> List<T> selectQuery(String query, Function<List<Object>, T> rowMapper, Collection<?> substitutions);

    java.sql.Date getCurrentDate();

}
