package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Mention;
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
public class MentionRepositoryImplTest {

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private SqlEntityMapper<Mention> mapper;

    @InjectMocks
    private MentionRepositoryImpl repository;

    @Before
    public void setUp() {
        when(mapper.toFields(any()))
                .thenReturn(List.of());
    }

    @Test
    public void testGet() {
        Mention mention = getMention(1);
        doAnswer(invocation -> {
            Function<List<Object>, Mention> fieldsMapper = invocation.getArgument(1, Function.class);
            fieldsMapper.apply(List.of());
            return List.of(mention);
        }).when(queryExecutor).selectQuery(eq("SELECT id, internal_message_id, chat_name FROM mention WHERE id IN (?);"), any(), eq(List.of(1L)));

        Mention clone = repository.get(1L);
        assertEquals(mention, clone);
        verify(mapper, times(1)).toObject(List.of());
    }

    @Test
    public void testGetNotFound() {
        Mention mention = getMention(1);
        when(queryExecutor.selectQuery(eq("SELECT id, internal_message_id, chat_name FROM mention WHERE id IN (?);"), any(), any()))
                .thenReturn(List.of());
        Mention clone = repository.get(mention.getId());
        assertNull(clone);
    }

    @Test
    public void testSave() {
        Mention mention = getMention(1);
        doAnswer(invocation -> {
            Function<Mention, List<Object>> objectMapper = invocation.getArgument(1, Function.class);
            Function<List<Object>, Long> idMapper = invocation.getArgument(3, Function.class);
            objectMapper.apply(mention);
            return List.of(idMapper.apply(List.of(2L)));
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("INSERT INTO mention (internal_message_id, chat_name) VALUES (?, ?);"), any(Function.class), eq(List.of(mention)), any(Function.class));
        assertTrue(repository.save(mention));
        assertEquals(Long.valueOf(2L), mention.getId());
        verify(mapper, times(1)).toFields(mention);
    }

    @Test
    public void testDeleteAllIds() {
        Mention mention = getMention(1);
        doAnswer(invocation -> {
            Function<Long, List<Object>> idMapper = invocation.getArgument(1, Function.class);
            assertEquals(List.of(1L), idMapper.apply(1L));
            return null;
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("DELETE FROM mention WHERE id = ?;"), any(Function.class), eq(List.of(mention.getId())), eq(null));

        boolean result = repository.delete(List.of(mention.getId()));
        assertTrue(result);
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM mention WHERE id = ?;"), any(Function.class), eq(List.of(mention.getId())), eq(null));
    }

    @Test
    public void testDeleteAll() {
        repository.delete();
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM mention;"), any(Function.class), eq(List.of()), eq(null));
    }

    @Nonnull
    private Mention getMention(int id) {
        final Mention mention = new Mention();
        mention.setId((long) id);
        return mention;
    }

}