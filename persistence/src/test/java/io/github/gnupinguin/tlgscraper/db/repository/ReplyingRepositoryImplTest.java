package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import io.github.gnupinguin.tlgscraper.model.db.Replying;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReplyingRepositoryImplTest {

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private SqlEntityMapper<Replying> mapper;

    @InjectMocks
    private ReplyingRepositoryImpl repository;

    @Before
    public void setUp() {
        when(mapper.toFields(any()))
                .thenReturn(List.of());
    }

    @Test
    public void testGet() {
        Replying replying = getReplying(1);
        doAnswer(invocation -> {
            Function<List<Object>, Replying> fieldsMapper = invocation.getArgument(1, Function.class);
            fieldsMapper.apply(List.of());
            return List.of(replying);
        }).when(queryExecutor).selectQuery(eq("SELECT internal_message_id, reply_to_message_id FROM replying WHERE internal_message_id IN (?);"), any(), eq(List.of(1L)));

        Replying clone = repository.get(1L);
        assertEquals(replying, clone);
        verify(mapper, times(1)).toObject(List.of());
    }

    @Test
    public void testGetNotFound() {
        Replying replying = getReplying(1);
        when(queryExecutor.selectQuery(eq("SELECT internal_message_id, reply_to_message_id FROM replying WHERE internal_message_id IN (?);"), any(), any()))
                .thenReturn(List.of());
        Replying clone = repository.get(replying.getMessage().getInternalId());
        assertNull(clone);
    }

    @Test
    public void testSave() {
        Replying replying = getReplying(1);
        doAnswer(invocation -> {
            Function<Replying, List<Object>> objectMapper = invocation.getArgument(1, Function.class);
            objectMapper.apply(replying);
            return List.of();
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("INSERT INTO replying (internal_message_id, reply_to_message_id) VALUES (?, ?);"), any(Function.class), eq(List.of(replying)), eq(null));
        assertTrue(repository.save(replying));
        verify(mapper, times(1)).toFields(replying);
    }

    @Test
    public void testSaveAll() {
        Replying replying1 = getReplying(1);
        Replying replying2 = getReplying(2);
        List<Replying> replyings = List.of(replying1, replying2);
        assertTrue(repository.save(replyings));
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("INSERT INTO replying (internal_message_id, reply_to_message_id) VALUES (?, ?);"), any(), eq(List.of(replying1, replying2)), eq(null));
    }

    @Test
    public void testDelete() {
        Replying replying = getReplying(1);
        boolean result = repository.delete(List.of(replying.getMessage().getInternalId()));
        assertTrue(result);
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM replying WHERE internal_message_id = ?;"), any(), eq(List.of(1L)), any());
    }

    @Test
    public void testDeleteAll() {
        repository.delete();
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM replying;"), any(), eq(List.of()), eq(null));
    }


    @Nonnull
    private Replying getReplying(long id) {
        return new Replying(Message.builder()
                .internalId(id)
                .build(), 1L);
    }

}