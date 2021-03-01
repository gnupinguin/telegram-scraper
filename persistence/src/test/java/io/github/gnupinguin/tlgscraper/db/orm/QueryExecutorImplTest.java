package io.github.gnupinguin.tlgscraper.db.orm;

import io.github.gnupinguin.tlgscraper.model.db.Link;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueryExecutorImplTest {

    @Mock
    private DbManager manager;

    @InjectMocks
    private QueryExecutorImpl executor;

    private Function<Link, List<Object>> serializer = link -> List.of(link.getMessage().getInternalId(), link.getUrl());
    private Function<List<Object>, Link> objectsToLinkMapper = objects -> new Link(Message.builder()
            .internalId((Long)objects.get(1))
            .build(), (Long)objects.get(0), (String)objects.get(2));

    private Function<List<Object>, Long> idMapper = list -> (Long)list.get(0);

    @Nonnull
    private Answer<Object> mockInvocation(PreparedStatement statement, ResultSet resultSet) {
        return invocation -> {
            var onStatement = (Consumer<PreparedStatement>) invocation.getArguments()[1];
            var resultMapper = (Function<ResultSet, Object>) invocation.getArguments()[2];
            onStatement.accept(statement);
            return resultMapper.apply(resultSet);
        };
    }

    @Test
    public void testUpdateAndMapResult() throws Exception {
        Link link = getLink(23);
        String query = "insert";

        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount())
                .thenReturn(2);
        when(resultSet.getMetaData())
                .thenReturn(metaData);

        when(resultSet.next())
                .thenReturn(true, true, false);
        Long firstId = link.getId();
        Long secondId = link.getId() + 1;
        when(resultSet.getObject(1))
                .thenReturn(firstId, secondId);
        when(resultSet.getObject(2))
                .thenReturn("fake", "field");
        when(manager.executeBatched(eq(query), any(), any()))
                .thenAnswer(mockInvocation(statement, resultSet));

        var ids = executor.batchedUpdateQuery(query, serializer, List.of(link, link), idMapper);

        verify(statement, times(2)).setObject(1, link.getMessage().getInternalId());
        verify(statement, times(2)).setObject(2, link.getUrl());
        verify(statement, times(2)).addBatch();

        assertEquals(firstId, ids.get(0));
        assertEquals(secondId, ids.get(1));
    }

    @Test
    public void testUpdateWithoutResultMapping() throws Exception {
        Link link = getLink(23);
        String query = "insert";

        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(manager.executeBatched(eq(query), any(), any()))
                .thenAnswer(mockInvocation(statement, resultSet));

        var ids = executor.batchedUpdateQuery(query, serializer, List.of(link, link), null);

        verify(statement, times(2)).setObject(1, link.getMessage().getInternalId());
        verify(statement, times(2)).setObject(2, link.getUrl());
        verify(statement, times(2)).addBatch();
        verifyZeroInteractions(resultSet);

        assertTrue(ids.isEmpty());
    }

    @Test
    public void testSelect() throws Exception {
        Link link1 = getLink(23);
        Link link2 = getLink(24);
        String query = "select";

        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount())
                .thenReturn(3);
        when(resultSet.getMetaData())
                .thenReturn(metaData);
        when(resultSet.next())
                .thenReturn(true, true, false);
        when(resultSet.getObject(1))
                .thenReturn(link1.getId(), link2.getId());
        when(resultSet.getObject(2))
                .thenReturn(link1.getMessage().getInternalId(), link2.getMessage().getInternalId());
        when(resultSet.getObject(3))
                .thenReturn(link1.getUrl(), link2.getUrl());
        when(manager.execute(eq(query), any(), any()))
                .thenAnswer(mockInvocation(statement, resultSet));
        var links = executor.selectQuery(query, objectsToLinkMapper, List.of(23L, 24L));
        verify(statement, times(1)).setObject(1, 23L);
        verify(statement, times(1)).setObject(2, 24L);

        assertEquals(2, links.size());
        assertEquals(link1, links.get(0));
        assertEquals(link2, links.get(1));
    }

    @Test
    public void testGetDate() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        java.util.Date date = new java.util.Date();

        when(resultSet.next())
                .thenReturn(true, false);
        when(resultSet.getDate(1))
                .thenReturn(new java.sql.Date(date.getTime()));

        when(manager.execute(eq("SELECT NOW();"), any(), any()))
                .thenAnswer( mockInvocation(statement, resultSet));
        Date currentDate = executor.getCurrentDate();

        assertEquals(date, currentDate);
        verifyNoMoreInteractions(statement);
    }

    private Link getLink(int id) {
        return new Link(Message.builder().internalId(1L).build(), (long)id, "url");
    }

}