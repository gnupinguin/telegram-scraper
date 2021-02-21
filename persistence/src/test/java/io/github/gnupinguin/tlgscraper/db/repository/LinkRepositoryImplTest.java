package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Link;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LinkRepositoryImplTest {

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private SqlEntityMapper<Link> mapper;

    @InjectMocks
    private LinkRepositoryImpl repository;

    @Before
    public void setUp() {
        when(mapper.toFields(any()))
                .thenReturn(List.of());
    }

    @Test
    public void testGet() {
        Link link = getLink(1);
        doAnswer(invocation -> {
            Function<List<Object>, Link> fieldsMapper = invocation.getArgument(1, Function.class);
            fieldsMapper.apply(List.of());
            return List.of(link);
        }).when(queryExecutor).selectQuery(eq("SELECT id, internal_message_id, url FROM link WHERE id IN (?);"), any(), eq(List.of(1L)));

        Link clone = repository.get(1L);
        assertEquals(link, clone);
        verify(mapper, times(1)).toObject(List.of());
    }

    @Test
    public void testGetNotFound() {
        Link link = getLink(1);
        when(queryExecutor.selectQuery(eq("SELECT id, internal_message_id, url FROM link WHERE id IN (?);"), any(), any()))
                .thenReturn(List.of());
        Link clone = repository.get(link.getId());
        assertNull(clone);
    }

    @Test
    public void testSave() {
        Link link = getLink(1);
        doAnswer(invocation -> {
            Function<Link, List<Object>> objectMapper = invocation.getArgument(1, Function.class);
            Function<List<Object>, Long> idMapper = invocation.getArgument(3, Function.class);
            objectMapper.apply(link);
            return List.of(idMapper.apply(List.of(2L)));
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("INSERT INTO link (internal_message_id, url) VALUES (?, ?);"), any(Function.class), eq(List.of(link)), any(Function.class));
        assertTrue(repository.save(link));
        assertEquals(Long.valueOf(2L), link.getId());
        verify(mapper, times(1)).toFields(link);
    }

    @Test
    public void testDeleteAllIds() {
        Link link = getLink(1);

        doAnswer(invocation -> {
            Function<Long, List<Object>> idMapper = invocation.getArgument(1, Function.class);
            assertEquals(List.of(1L), idMapper.apply(1L));
            return null;
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("DELETE FROM link WHERE id = ?;"), any(Function.class), eq(List.of(link.getId())), eq(null));

        boolean result = repository.delete(List.of(link.getId()));
        assertTrue(result);
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM link WHERE id = ?;"), any(Function.class), eq(List.of(link.getId())), eq(null));
    }

    @Test
    public void testDeleteAll() {
        repository.delete();
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM link;"), any(Function.class), eq(List.of()), eq(null));
    }

    @Nonnull
    private Link getLink(int id) {
        final Link link = new Link();
        link.setId((long) id);
        return link;
    }

}