package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Mention;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class MentionSqlEntityMapper implements SqlEntityMapper<Mention> {

    @Override
    public @Nonnull List<Object> toFields(@Nonnull Mention entity) {
        return List.of(entity.getInternalMessageId(), entity.getChatName());
    }

    @Override
    @Nullable
    public Mention toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return new Mention((Long) fields.get(0),
                    (Long) fields.get(1),
                    (String) fields.get(2));
        }
        return null;
    }

}
