package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.Link;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class LinkSqlEntityMapperTest {

    @InjectMocks
    private LinkSqlEntityMapper mapper;

    private Link link = new Link(1L, 2L, "url");

    @Test
    public void testToFields() {
        List<Object> fields = mapper.toFields(link);
        assertEquals(2, fields.size());
        assertEquals(link.getInternalMessageId(), fields.get(0));
        assertEquals(link.getUrl(), fields.get(1));
    }

    @Test
    public void testToObject() {
        Link result = mapper.toObject(List.of(link.getId(), link.getInternalMessageId(), link.getUrl()));
        assertEquals(link, result);
    }

    @Test
    public void testEmptyFields() {
        assertNull(mapper.toObject(List.of()));
    }

}