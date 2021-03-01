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
        return List.of(entity.getName(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getMembers());
    }

    @Override
    @Nullable
    public Chat toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return Chat.builder()
                    .id((Long) fields.get(0))
                    .name((String) fields.get(1))
                    .title((String) fields.get(2))
                    .description((String) fields.get(3))
                    .members((Integer) fields.get(4))
                    .build();
        }
        return null;
    }

}
