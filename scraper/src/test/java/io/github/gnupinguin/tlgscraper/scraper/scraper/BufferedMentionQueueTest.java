package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.db.queue.MentionTask;
import io.github.gnupinguin.tlgscraper.db.queue.StatusTaskQueue;
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import io.github.gnupinguin.tlgscraper.scraper.persistence.BufferedMentionQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BufferedMentionQueueTest {

    @Mock
    private StatusTaskQueue<MentionTask> repository;

    @InjectMocks
    private BufferedMentionQueue mentionQueue;

    @Test
    public void testPoll() {
        when(repository.poll(20))
                .thenReturn(List.of(mention("chat1"), mention("chat2")));
        assertEquals("chat1", mentionQueue.poll());
        assertEquals("chat2", mentionQueue.poll());
    }

    @Test
    public void testClearOldBeforeRefresh() {
        when(repository.poll(20))
                .thenReturn(List.of(mention("chat1")),
                        List.of(mention("chat2"), mention("chat3")));

        doAnswer(answer -> {
            List<MentionTask> list = new ArrayList<>(answer.getArgument(0));
            assertEquals(1, list.size());
            assertEquals(new MentionTask(TaskStatus.SuccessfullyProcessed, "chat1"), list.get(0));
            return null;
        }).when(repository).updateStatuses(anyCollection());

        assertEquals("chat1", mentionQueue.poll());
        assertEquals("chat2", mentionQueue.poll());
        assertEquals("chat3", mentionQueue.poll());

        verify(repository, times(2)).poll(20);
    }

    @Test
    public void testNullForEndOfQueue() {
        when(repository.poll(20))
                .thenReturn(List.of(mention("chat1")), List.of());
        assertEquals("chat1", mentionQueue.poll());
        assertNull(mentionQueue.poll());
    }

    private MentionTask mention(String name) {
        return new MentionTask(TaskStatus.Initial, name);
    }

}