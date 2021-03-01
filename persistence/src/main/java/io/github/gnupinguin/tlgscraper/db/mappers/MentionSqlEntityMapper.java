package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Mention;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class MentionSqlEntityMapper implements SqlEntityMapper<Mention> {

    @Override
    public @Nonnull List<Object> toFields(@Nonnull Mention entity) {
        return List.of(entity.getMessage().getInternalId(), entity.getChatName());
    }

    @Override
    @Nullable
    public Mention toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return Mention.builder()
                    .id((Long) fields.get(0))
                    .message(Message.builder()
                            .internalId((Long)fields.get(1))//TODO investigate
                            .build())
                    .chatName((String) fields.get(2))
                    .build();
        }
        return null;
    }

}
