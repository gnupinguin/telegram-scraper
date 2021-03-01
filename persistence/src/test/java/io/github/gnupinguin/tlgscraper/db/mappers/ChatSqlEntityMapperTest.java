package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ChatSqlEntityMapperTest {

    @InjectMocks
    private ChatSqlEntityMapper mapper;

    private Chat entity = new Chat(1L, "name", "title", "description", 32, null);

    @Test
    public void testToFields() {
        List<Object> fields = mapper.toFields(entity);

        assertEquals(4, fields.size());
        assertEquals(entity.getName(), fields.get(0));
        assertEquals(entity.getTitle(), fields.get(1));
        assertEquals(entity.getDescription(), fields.get(2));
        assertEquals(entity.getMembers(), fields.get(3));
    }

    @Test
    public void testToObject() {
        Chat chat = mapper.toObject(List.of(1L, "name", "title", "description", 32));
        assertEquals(entity, chat);
    }

    @Test
    public void testEmptyFields() {
        assertNull(mapper.toObject(List.of()));
    }

}