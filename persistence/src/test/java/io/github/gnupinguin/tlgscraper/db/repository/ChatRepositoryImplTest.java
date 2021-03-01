package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.model.db.Chat;
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
public class ChatRepositoryImplTest {

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private SqlEntityMapper<Chat> mapper;

    @InjectMocks
    private ChatRepositoryImpl repository;

    @Before
    public void setUp() {
        when(mapper.toFields(any()))
                .thenReturn(List.of());
    }

    @Test
    public void testGet() {
        Chat chat = getChat(1);
        doAnswer(invocation -> {
            Function<List<Object>, Chat> fieldsMapper = invocation.getArgument(1, Function.class);
            fieldsMapper.apply(List.of());
            return List.of(chat);
        }).when(queryExecutor).selectQuery(eq("SELECT id, name, title, description, members FROM chat WHERE id IN (?);"), any(), eq(List.of(1L)));

        Chat clone = repository.get(1L);
        assertEquals(chat, clone);
        verify(mapper, times(1)).toObject(List.of());
    }

    @Test
    public void testGetNotFound() {
        Chat chat = getChat(1);
        when(queryExecutor.selectQuery(eq("SELECT id, name, title, description, members FROM chat WHERE id IN (?);"), any(), any()))
                .thenReturn(List.of());
        Chat clone = repository.get(chat.getId());
        assertNull(clone);
    }

    @Test
    public void testSave() {
        Chat chat = getChat(1);
        doAnswer(invocation -> {
//            Function<Chat, List<Object>> objectMapper = invocation.getArgument(1, Function.class);
//            objectMapper.apply(chat);
//            return List.of();
            Function<Chat, List<Object>> objectMapper = invocation.getArgument(1, Function.class);
            Function<List<Object>, Long> idMapper = invocation.getArgument(3, Function.class);
            objectMapper.apply(chat);
            return List.of(idMapper.apply(List.of(2L)));
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("INSERT INTO chat (name, title, description, members) VALUES (?, ?, ?, ?);"), any(Function.class), eq(List.of(chat)), any(Function.class));
        assertTrue(repository.save(chat));
        assertEquals(Long.valueOf(2L), chat.getId());
        verify(mapper, times(1)).toFields(chat);
    }

    @Test
    public void testSaveAll() {
        Chat chat1 = getChat(1);
        Chat chat2 = getChat(2);
        List<Chat> chatList = List.of(chat1, chat2);
        assertTrue(repository.save(chatList));
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("INSERT INTO chat (name, title, description, members) VALUES (?, ?, ?, ?);"), any(), eq(List.of(chat1, chat2)), any(Function.class));
    }

    @Test
    public void testDelete() {
        Chat chat = getChat(1);
        boolean result = repository.delete(List.of(chat.getId()));
        assertTrue(result);
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM chat WHERE id = ?;"), any(), eq(List.of(1L)), any());
    }

    @Test
    public void testDeleteAll() {
        repository.delete();
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM chat;"), any(), eq(List.of()), eq(null));
    }


    @Nonnull
    private Chat getChat(int id) {
        final Chat chat = new Chat();
        chat.setId((long) id);
        return chat;
    }

}