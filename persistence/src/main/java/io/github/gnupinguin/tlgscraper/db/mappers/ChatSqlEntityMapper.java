package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class ChatSqlEntityMapper implements SqlEntityMapper<Chat> {

    @Nonnull
    @Override
    public List<Object> toFields(@Nonnull Chat entity) {
        return List.of(entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getMembers());
    }

    @Override
    @Nullable
    public Chat toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return new Chat((Long) fields.get(0),
                    (String) fields.get(1),
                    (String) fields.get(2),
                    (Integer) fields.get(3));
        }
        return null;
    }

}
