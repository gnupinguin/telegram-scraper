package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.HashTag;
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
public class HashTagRepositoryImplTest {

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private SqlEntityMapper<HashTag> mapper;

    @InjectMocks
    private HashTagRepositoryImpl repository;

    @Before
    public void setUp() {
        when(mapper.toFields(any()))
                .thenReturn(List.of());
    }

    @Test
    public void testGet() {
        HashTag hashTag = getHashTag(1);
        doAnswer(invocation -> {
            Function<List<Object>, HashTag> fieldsMapper = invocation.getArgument(1, Function.class);
            fieldsMapper.apply(List.of());
            return List.of(hashTag);
        }).when(queryExecutor).selectQuery(eq("SELECT id, internal_message_id, tag FROM hashtag WHERE id IN (?);"), any(), eq(List.of(1L)));

        HashTag clone = repository.get(1L);
        assertEquals(hashTag, clone);
        verify(mapper, times(1)).toObject(List.of());
    }

    @Test
    public void testGetNotFound() {
        HashTag hashTag = getHashTag(1);
        when(queryExecutor.selectQuery(eq("SELECT id, internal_message_id, tag FROM hashtag WHERE id IN (?);"), any(), any()))
                .thenReturn(List.of());
        HashTag clone = repository.get(hashTag.getId());
        assertNull(clone);
    }

    @Test
    public void testSave() {
        HashTag hashTag = getHashTag(1);
        doAnswer(invocation -> {
            Function<HashTag, List<Object>> objectMapper = invocation.getArgument(1, Function.class);
            Function<List<Object>, Long> idMapper = invocation.getArgument(3, Function.class);
            objectMapper.apply(hashTag);
            return List.of(idMapper.apply(List.of(2L)));
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("INSERT INTO hashtag (internal_message_id, tag) VALUES (?, ?);"), any(Function.class), eq(List.of(hashTag)), any(Function.class));
        assertTrue(repository.save(hashTag));
        assertEquals(Long.valueOf(2L), hashTag.getId());
        verify(mapper, times(1)).toFields(hashTag);
    }

    @Test
    public void testDelete() {
        HashTag hashTag = getHashTag(1);
        doAnswer(invocation -> {
            Function<Long, List<Object>> idMapper = invocation.getArgument(1, Function.class);
            assertEquals(List.of(1L), idMapper.apply(1L));
            return null;
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("DELETE FROM hashtag WHERE id = ?;"), any(Function.class), eq(List.of(hashTag.getId())), eq(null));

        boolean result = repository.delete(List.of(hashTag.getId()));
        assertTrue(result);
    }

    @Test
    public void testDeleteAll() {
        repository.delete();
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM hashtag;"), any(Function.class), eq(List.of()), eq(null));
    }

    @Nonnull
    private HashTag getHashTag(int id) {
        final HashTag hashTag = new HashTag();
        hashTag.setId((long) id);
        return hashTag;
    }

}