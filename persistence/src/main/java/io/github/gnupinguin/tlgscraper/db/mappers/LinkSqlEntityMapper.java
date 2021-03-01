package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Link;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class LinkSqlEntityMapper implements SqlEntityMapper<Link> {

    @Override
    public @Nonnull List<Object> toFields(@Nonnull Link entity) {
        return List.of(entity.getMessage().getInternalId(), entity.getUrl());
    }

    @Override
    @Nullable
    public Link toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return Link.builder()
                    .id((Long) fields.get(0))
                    .message(Message.builder()
                            .internalId((Long)fields.get(1))//TODO investigate
                            .build())
                    .url((String) fields.get(2))
                    .build();
        }
        return null;
    }

}
