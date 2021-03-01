package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.HashTag;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class HashTagSqlEntityMapper implements SqlEntityMapper<HashTag> {

    @Override
    public @Nonnull List<Object> toFields(@Nonnull HashTag entity) {
        return List.of(entity.getMessage().getInternalId(), entity.getTag());
    }

    @Override
    @Nullable
    public HashTag toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return HashTag.builder()
                    .id((Long) fields.get(0))
                    .message(Message.builder()
                            .internalId((Long)fields.get(1))//TODO investigate
                            .build())
                    .tag((String) fields.get(2))
                    .build();
        }
        return null;
    }

}
