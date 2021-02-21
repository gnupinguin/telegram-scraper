package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Mention;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class MentionSqlEntityMapperTest {

    @InjectMocks
    private MentionSqlEntityMapper mapper;

    private Mention mention = new Mention(1L, 2L, "chat");

    @Test
    public void testToFields() {
        List<Object> fields = mapper.toFields(mention);

        assertEquals(2, fields.size());
        assertEquals(mention.getInternalMessageId(), fields.get(0));
        assertEquals(mention.getChatName(), fields.get(1));
    }

    @Test
    public void testToObject() {
        Mention result = mapper.toObject(List.of(mention.getId(), mention.getInternalMessageId(), mention.getChatName()));

        assertEquals(mention, result);
    }

    @Test
    public void testEmptyFields() {
        assertNull(mapper.toObject(List.of()));
    }

}