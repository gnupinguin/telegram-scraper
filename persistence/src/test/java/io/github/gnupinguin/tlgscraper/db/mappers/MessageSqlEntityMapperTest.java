package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Message;
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

    private Message message = new Message(1L, 2L, 3L, 4L,
                                5L, 6L, 1, "hello",
                                new Timestamp(1L), new Timestamp(2L), 3);

    @Test
    public void testToFields() {
        List<Object> fields = mapper.toFields(message);

        assertEquals(10, fields.size());
        assertEquals(message.getChatId(), fields.get(0));
        assertEquals(message.getMessageId(), fields.get(1));
        assertEquals(message.getReplyToMessageId(), fields.get(2));
        assertEquals(message.getForwardedFromChatId(), fields.get(3));
        assertEquals(message.getForwardedFromMessageId(), fields.get(4));
        assertEquals(message.getType(), fields.get(5));
        assertEquals(message.getTextContent(), fields.get(6));
        assertEquals(message.getPublishDate(), fields.get(7));
        assertEquals(message.getLoadDate(), fields.get(8));
        assertEquals(message.getViews(), fields.get(9));
    }

    @Test
    public void testToObject() {
        List<Object> fields = List.of(
                message.getInternalId(),
                message.getChatId(),
                message.getMessageId(),
                message.getReplyToMessageId(),
                message.getForwardedFromChatId(),
                message.getForwardedFromMessageId(),
                message.getType(),
                message.getTextContent(),
                message.getPublishDate(),
                message.getLoadDate(),
                message.getViews());
        Message result = mapper.toObject(fields);

        assertEquals(message, result);
    }

    @Test
    public void testEmptyFields() {
        assertNull(mapper.toObject(List.of()));
    }

}