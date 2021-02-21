package io.github.gnupinguin.tlgscraper.db.mappers;

import io.github.gnupinguin.tlgscraper.model.db.HashTag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class HashTagSqlEntityMapperTest {

    @InjectMocks
    private HashTagSqlEntityMapper mapper;

    private HashTag tag = new HashTag(1L, 2L, "tag");

    @Test
    public void testToFields() {

        List<Object> fields = mapper.toFields(tag);
        assertEquals(2, fields.size());
        assertEquals(tag.getInternalMessageId(), fields.get(0));
        assertEquals(tag.getTag(), fields.get(1));
    }

    @Test
    public void testToObject() {
        List<Object> fields = List.of(tag.getId(), tag.getInternalMessageId(), tag.getTag());
        HashTag result = mapper.toObject(fields);
        assertEquals(tag, result);
    }

    @Test
    public void testEmptyFields() {
        assertNull(mapper.toObject(List.of()));
    }

}