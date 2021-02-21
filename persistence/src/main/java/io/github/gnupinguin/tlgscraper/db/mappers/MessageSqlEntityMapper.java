package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Message;
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
                entity.getChatId(),
                entity.getMessageId(),
                entity.getReplyToMessageId(),
                entity.getForwardedFromChatId(),
                entity.getForwardedFromMessageId(),
                entity.getType(),
                entity.getTextContent(),
                entity.getPublishDate(),
                entity.getLoadDate(),
                entity.getViews()
        );
    }

    @Override
    public @Nullable Message toObject(@Nonnull List<Object> fields) {
        if (!fields.isEmpty()) {
            Message message = new Message();
            message.setInternalId((Long) fields.get(0));
            message.setChatId((Long) fields.get(1));
            message.setMessageId((Long) fields.get(2));
            message.setReplyToMessageId((Long) fields.get(3));
            message.setForwardedFromChatId((Long) fields.get(4));
            message.setForwardedFromMessageId((Long) fields.get(5));
            message.setType((Integer) fields.get(6));
            message.setTextContent((String) fields.get(7));
            message.setPublishDate((Timestamp) fields.get(8));
            message.setLoadDate((Timestamp) fields.get(9));
            message.setViews((Integer) fields.get(10));
            return message;
        }
        return null;
    }

}
