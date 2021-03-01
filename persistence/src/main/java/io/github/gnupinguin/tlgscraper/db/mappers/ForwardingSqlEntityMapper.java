package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Forwarding;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class ForwardingSqlEntityMapper implements SqlEntityMapper<Forwarding> {

    @Nonnull
    @Override
    public List<Object> toFields(@Nonnull Forwarding entity) {
        return List.of(
                entity.getMessage().getInternalId(),
                entity.getForwardedFromChannel(),
                entity.getForwardedFromMessageId()
        );
    }

    @Nullable
    @Override
    public Forwarding toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return Forwarding.builder()
                    .message(Message.builder()
                            .internalId((Long)fields.get(0))//TODO investigate
                            .build())
                    .forwardedFromChannel((String)fields.get(1))
                    .forwardedFromMessageId((Long)fields.get(2))
                    .build();
        }

        return null;
    }
}
