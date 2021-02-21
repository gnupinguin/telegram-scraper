package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.mappers.SqlEntityMapper;
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageRepositoryTest {

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private SqlEntityMapper<Message> mapper;

    @InjectMocks
    private MessageRepositoryImpl repository;

    @Before
    public void setUp() {
        when(mapper.toFields(any()))
                .thenReturn(List.of());
    }

    @Test
    public void testGet() {
        Message message = getMessage(1);
        doAnswer(invocation -> {
            Function<List<Object>, Message> fieldsMapper = invocation.getArgument(1, Function.class);
            fieldsMapper.apply(List.of());
            return List.of(message);
        }).when(queryExecutor).selectQuery(eq(selectWhere("internal_id IN (?);")), any(), eq(List.of(1L)));

        Message clone = repository.get(1L);
        assertEquals(message, clone);
        verify(mapper, times(1)).toObject(List.of());
    }

    @Test
    public void testGetNotFound() {
        Message message = getMessage(1);
        when(queryExecutor.selectQuery(eq(selectWhere("internal_id IN (?);")), any(), any()))
                .thenReturn(List.of());
        Message clone = repository.get(message.getInternalId());
        assertNull(clone);
    }

    @Test
    public void testSave() {
        Message message = getMessage(1);
        doAnswer(invocation -> {
            Function<Message, List<Object>> objectMapper = invocation.getArgument(1, Function.class);
            Function<List<Object>, Long> idMapper = invocation.getArgument(3, Function.class);
            objectMapper.apply(message);
            return List.of(idMapper.apply(List.of(2L)));
        }).when(queryExecutor)
                .batchedUpdateQuery(eq(insertQuery()), any(Function.class), eq(List.of(message)), any(Function.class));
        assertTrue(repository.save(message));
        assertEquals(Long.valueOf(2L), message.getInternalId());
        verify(mapper, times(1)).toFields(message);
    }

    @Test
    public void testDeleteAllIds() {
        Message message = getMessage(1);
        doAnswer(invocation -> {
            Function<Long, List<Object>> idMapper = invocation.getArgument(1, Function.class);
            assertEquals(List.of(1L), idMapper.apply(1L));
            return null;
        }).when(queryExecutor)
                .batchedUpdateQuery(eq("DELETE FROM message WHERE internal_id = ?;"), any(Function.class), eq(List.of(message.getInternalId())), eq(null));

        boolean result = repository.delete(List.of(message.getInternalId()));
        assertTrue(result);
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM message WHERE internal_id = ?;"), any(Function.class), eq(List.of(message.getInternalId())), eq(null));

    }

    @Test
    public void testDeleteAll() {
        repository.delete();
        verify(queryExecutor, times(1))
                .batchedUpdateQuery(eq("DELETE FROM message;"), any(Function.class), eq(List.of()), eq(null));
    }

    @Test
    public void testSelectForChat() {
        Message message = getMessage(1);
        List<Message> messageList = List.of(message);
        when(queryExecutor.selectQuery(eq(selectWhere("chat_id = ?;")), any(Function.class), eq(List.of(message.getChatId()))))
                .thenReturn(messageList);
        List<Message> messages = repository.getForChat(message.getChatId());
        assertEquals(messageList, messages);
    }

    @Nonnull
    private Message getMessage(int id) {
        final Message message = new Message();
        message.setInternalId((long) id);
        message.setChatId(32L);
        return message;
    }

    @Nonnull
    private String selectWhere(String condition) {
        return "SELECT internal_id, chat_id, message_id, " +
                "reply_to_message_id, forwarded_from_chat_id, " +
                "forwarded_from_message_id, type, text_content, " +
                "publish_date, load_date, views FROM message WHERE " + condition;
    }

    @Nonnull
    private String insertQuery() {
        return "INSERT INTO message" +
                " (chat_id, " +
                "message_id, " +
                "reply_to_message_id, " +
                "forwarded_from_chat_id, " +
                "forwarded_from_message_id, " +
                "type, " +
                "text_content, " +
                "publish_date, " +
                "load_date, " +
                "views) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    }

}