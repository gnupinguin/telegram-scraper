package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import io.github.gnupinguin.tlgscraper.model.scraper.web.MessageType;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Component
public class MessageSqlEntityMapper implements SqlEntityMapper<Message> {

    @Override
    public @Nonnull List<Object> toFields(@Nonnull Message entity) {
        return Arrays.asList(
                entity.getChannel().getId(),
                entity.getId(),
                entity.getType().getTypeId(),
                entity.getTextContent(),
                entity.getPublishDate(),
                entity.getLoadDate(),
                entity.getViewCount()
        );
    }

    @Override
    public @Nullable Message toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            return Message.builder()
                    .internalId((Long) fields.get(0))
                    .channel(Chat.builder()
                            .id((Long) fields.get(1))
                            .build())
                    .id((Long) fields.get(2))
                    .type(MessageType.parse((Integer) fields.get(3)))
                    .textContent((String) fields.get(4))
                    .publishDate((Timestamp) fields.get(5))
                    .loadDate((Timestamp) fields.get(6))
                    .viewCount((Integer) fields.get(7))
                    .build();
        }
        return null;
    }

}
