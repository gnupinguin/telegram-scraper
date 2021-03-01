package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Forwarding;
import io.github.gnupinguin.tlgscraper.model.db.Message;
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
public class ForwardingRepositoryImplTest {

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private SqlEntityMapper<Forwarding> mapper;

    @InjectMocks
    private ForwardingRepositoryImpl repository;

    @Before
    public void setUp() {
        when(mapper.toFields(any()))
                .thenReturn(List.of());
    }

    @Test
    public void testGet() {
        Forwarding forwarding = getForwarding(1);
        doAnswer(invocation -> {
            Function<List<Object>, Forwarding> fieldsMapper = invocation.getArgument(1, Function.class);
            fieldsMapper.apply(List.of());
            return List.of(forwarding);
        }).when(queryExecutor).selectQuery(eq("SELECT internal_message_id, forwarded_from_channel, forwarded_from_message_id FROM forwarding WHERE internal_message_id IN (?);"), any(), eq(List.of(1L)));

        Forwarding clone = repository.get(1L);
        assertEquals(forwarding, clone);
        verify(mapper, times(1)).toObject(List.of());
    }

    @Test
    public void testGetNotFound() {
        Forwarding forwarding = getForwarding(1);
        when(queryExecutor.selectQuery(eq("SELECT internal_message_id, forwarded_from_channel, forwarded_from_message_id FROM forwarding WHERE internal_message_id IN (?);"), any(), any()))
                .thenReturn(List.of());
        Forwarding clone = repository.get(forwarding.getMessage().getInternalId());
        assertNull(clone);
    }

    @Test
    public void testSave() {
        Forwarding forwarding = getForwarding(1);
        doAnswer(invocation -> {
            Function<Forwarding, List<Object>> objectMapper = invocation.getArgument(1, Function.class);
            objectMapper.apply(forwarding);
            return List.of();
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("INSERT INTO forwarding (internal_message_id, forwarded_from_channel, forwarded_from_message_id) VALUES (?, ?, ?);"), any(Function.class), eq(List.of(forwarding)), eq(null));
        assertTrue(repository.save(forwarding));
        verify(mapper, times(1)).toFields(forwarding);
    }

    @Test
    public void testSaveAll() {
        Forwarding forwarding1 = getForwarding(1);
        Forwarding forwarding2 = getForwarding(2);
        List<Forwarding> forwardings = List.of(forwarding1, forwarding2);
        assertTrue(repository.save(forwardings));
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("INSERT INTO forwarding (internal_message_id, forwarded_from_channel, forwarded_from_message_id) VALUES (?, ?, ?);"), any(), eq(List.of(forwarding1, forwarding2)), eq(null));
    }

    @Test
    public void testDelete() {
        Forwarding forwarding = getForwarding(1);
        boolean result = repository.delete(List.of(forwarding.getMessage().getInternalId()));
        assertTrue(result);
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM forwarding WHERE internal_message_id = ?;"), any(), eq(List.of(1L)), any());
    }

    @Test
    public void testDeleteAll() {
        repository.delete();
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM forwarding;"), any(), eq(List.of()), eq(null));
    }


    @Nonnull
    private Forwarding getForwarding(long id) {
        return new Forwarding(Message.builder()
                .internalId(id)
                .build(), "chat", 1L);
    }

}