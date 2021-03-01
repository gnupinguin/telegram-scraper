package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Message;
import io.github.gnupinguin.tlgscraper.model.db.Replying;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class ReplyingSqlEntityMapper implements SqlEntityMapper<Replying> {

    @Nonnull
    @Override
    public List<Object> toFields(@Nonnull Replying entity) {
        return List.of(
                entity.getMessage().getInternalId(),
                entity.getReplyToMessageId()
        );
    }

    @Nullable
    @Override
    public Replying toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return Replying.builder()
                    .message(Message.builder()
                            .internalId((Long) fields.get(0))
                            .build())
                    .replyToMessageId((Long) fields.get(1))
                    .build();
        }
        return null;
    }

}
