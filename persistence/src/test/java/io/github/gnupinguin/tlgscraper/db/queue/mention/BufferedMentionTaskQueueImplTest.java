package io.github.gnupinguin.tlgscraper.db.queue.mention;

import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BufferedMentionTaskQueueImplTest {

    @Mock
    private MentionTaskQueue repository;

    @InjectMocks
    private BufferedMentionTaskQueueImpl mentionQueue;

    @Test
    public void testPoll() {
        MentionTask m1 = mention("chat1");
        MentionTask m2 = mention("chat2");
        when(repository.poll(20))
                .thenReturn(List.of(m1, m2));
        assertEquals(m1, mentionQueue.poll());
        assertEquals(m2, mentionQueue.poll());
    }

    @Test
    public void testClearOldBeforeRefresh() {
        MentionTask m1 = mention("chat1");
        MentionTask m2 = mention("chat2");
        MentionTask m3 = mention("chat3");
        when(repository.poll(20))
                .thenReturn(List.of(m1), List.of(m2, m3));

        doAnswer(answer -> {
            List<MentionTask> list = new ArrayList<>(answer.getArgument(0));
            assertEquals(1, list.size());
            assertEquals(new MentionTask(TaskStatus.SuccessfullyProcessed, "chat1"), list.get(0));
            return null;
        }).when(repository).update(anyCollection());

        assertEquals(m1, mentionQueue.poll());
        assertEquals(m2, mentionQueue.poll());
        assertEquals(m3, mentionQueue.poll());

        verify(repository, times(2)).poll(20);
    }

    @Test
    public void testNullForEndOfQueue() {
        MentionTask m1 = mention("chat1");
        when(repository.poll(20))
                .thenReturn(List.of(m1), List.of());
        assertEquals(m1, mentionQueue.poll());
        assertNull(mentionQueue.poll());
    }

    private MentionTask mention(String name) {
        return new MentionTask(TaskStatus.Initial, name);
    }

}