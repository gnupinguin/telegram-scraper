package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.HashTag;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class HashTagSqlEntityMapper implements SqlEntityMapper<HashTag> {

    @Override
    public @Nonnull List<Object> toFields(@Nonnull HashTag entity) {
        return List.of(entity.getInternalMessageId(), entity.getTag());
    }

    @Override
    @Nullable
    public HashTag toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return new HashTag((Long) fields.get(0),
                    (Long) fields.get(1),
                    (String) fields.get(2));
        }
        return null;
    }

}
