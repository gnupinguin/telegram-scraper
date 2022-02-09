package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import io.github.gnupinguin.tlgscraper.model.scraper.web.MessageType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class MessageSqlEntityMapperTest {

    @InjectMocks
    private MessageSqlEntityMapper mapper;

    private Message message = Message.builder()
            .internalId(0L)
            .id(1L)
            .channel(Chat.builder()
                    .id(3L)
                    .build())
            .type(MessageType.Text)
            .textContent("hello")
            .viewCount(32)
            .loadDate(new Timestamp(1L))
            .publishDate(new Timestamp(1L))
            .build();
    @Test
    public void testToFields() {
        List<Object> fields = mapper.toFields(message);

        assertEquals(7, fields.size());
        assertEquals(message.getChannel().getId(), fields.get(0));
        assertEquals(message.getId(), fields.get(1));
        assertEquals(message.getType().getTypeId(), fields.get(2));
        assertEquals(message.getTextContent(), fields.get(3));
        assertEquals(message.getPublishDate(), fields.get(4));
        assertEquals(message.getLoadDate(), fields.get(5));
        assertEquals(message.getViewCount(), fields.get(6));
    }

    @Test
    public void testToObject() {
        List<Object> fields = List.of(
                message.getInternalId(),
                message.getChannel().getId(),
                message.getId(),
                message.getType().getTypeId(),
                message.getTextContent(),
                message.getPublishDate(),
                message.getLoadDate(),
                message.getViewCount());
        Message result = mapper.toObject(fields);

        assertEquals(message, result);
    }

    @Test
    public void testEmptyFields() {
        assertNull(mapper.toObject(List.of()));
    }

}