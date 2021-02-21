package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Link;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class LinkSqlEntityMapper implements SqlEntityMapper<Link> {

    @Override
    public @Nonnull List<Object> toFields(@Nonnull Link entity) {
        return List.of(entity.getInternalMessageId(), entity.getUrl());
    }

    @Override
    @Nullable
    public Link toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return new Link((Long) fields.get(0),
                    (Long) fields.get(1),
                    (String) fields.get(2));
        }
        return null;
    }

}
