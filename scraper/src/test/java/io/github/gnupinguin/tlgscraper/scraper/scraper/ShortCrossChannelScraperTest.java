package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.notification.Notificator;
import io.github.gnupinguin.tlgscraper.scraper.persistence.ApplicationStorage;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Mention;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask.TaskStatus;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Message;
import io.github.gnupinguin.tlgscraper.scraper.queue.MentionTaskQueue;
import io.github.gnupinguin.tlgscraper.scraper.scraper.filter.ChannelFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShortCrossChannelScraperTest {

    private static final String CHANNEL_NAME = "channel";

    public static final String MENTION1 = "channel1";
    public static final String MENTION2 = "channel2";

    @Mock
    private ChannelScrapper channelScrapper;

    @Mock
    private MentionTaskQueue mentionQueue;

    @Mock
    private ApplicationStorage storage;

    @Mock
    private ChannelFilter filter;

    @Mock
    private Notificator notificator;

    @Mock
    private ScraperConfiguration configuration;

    @InjectMocks
    private ShortCrossChannelScraper crossChatScrapper;

    private Channel channel;

    private MentionTask mentionTask;

    @Before
    public void setUp() {
        when(configuration.getMaxFailures())
                .thenReturn(1);
        when(configuration.getChannelMessagesCount())
                .thenReturn(1);
        when(configuration.getFetchingMentionTasksCount())
                .thenReturn(1);

        channel = Channel.builder().name(CHANNEL_NAME).build();
        List<Message> messages= List.of(message(MENTION1), message(MENTION2));
        channel.setMessages(messages);

        mentionTask = initialMentionTask(CHANNEL_NAME);

        when(channelScrapper.scrap(CHANNEL_NAME, configuration.getChannelMessagesCount()))
                .thenReturn(channel);

        when(mentionQueue.poll(configuration.getFetchingMentionTasksCount()))
                .thenReturn(List.of(mentionTask), List.of());

        when(filter.doFilter(channel))
                .thenReturn(true);
        when(notificator.approveRestoration(anyCollection()))
                .thenReturn(true);
    }

    private MentionTask processedMentionTask(String name) {
        return new MentionTask(name,TaskStatus.SuccessfullyProcessed);
    }

    private MentionTask initialMentionTask(String name) {
        return new MentionTask(name, TaskStatus.Initial);
    }

    @Test
    public void testScrapFromQueue() {
        crossChatScrapper.scrap();

        verify(storage, times(1)).save(channel);
        verify(mentionQueue, times(1))
                .update(List.of(initialMentionTask(MENTION1), initialMentionTask(MENTION2), processedMentionTask(CHANNEL_NAME)));
        verify(mentionQueue, times(2)).poll(configuration.getFetchingMentionTasksCount());
    }

    @Test
    public void testChannelNotFound() {
        when(channelScrapper.scrap(CHANNEL_NAME, configuration.getChannelMessagesCount()))
                .thenReturn(null);
        crossChatScrapper.scrap();
        verify(storage, never()).save(any());
        verify(mentionQueue, times(1)).update(isTaskWithStatus(mentionTask, TaskStatus.InvalidProcessed));
        verify(mentionQueue, times(2)).poll(configuration.getFetchingMentionTasksCount());
    }

    private List<MentionTask> isTaskWithStatus(MentionTask task, TaskStatus status) {
        return argThat(list -> list.get(0).getStatus() == status &&
                list.get(0).getName().equals(task.getName()));
    }


    @Test
    public void testStopAfterFailures() {
        when(mentionQueue.poll(configuration.getFetchingMentionTasksCount()))
                .thenReturn(List.of(mentionTask));
        when(channelScrapper.scrap(CHANNEL_NAME, configuration.getChannelMessagesCount()))
                .thenReturn(null);
        crossChatScrapper.scrap();
        verify(storage, never()).save(any());

        //configuration.getFetchingMentionTasksCount() + 1 with current chat
        verify(mentionQueue, times(configuration.getMaxFailures()))
                .update(isTaskWithStatus(mentionTask, TaskStatus.InvalidProcessed));
        verify(mentionQueue, times(configuration.getMaxFailures()+1))
                .poll(configuration.getFetchingMentionTasksCount());
        verify(mentionQueue, times(1))
                .restore(Collections.nCopies(configuration.getMaxFailures()+1, mentionTask));
    }

    @Test
    public void testContinueAfterFailuresButRestorationNotApproved() {
        var counter = new LongAccumulator(Long::sum, 0);
        when(mentionQueue.poll(configuration.getFetchingMentionTasksCount()))
                .thenAnswer(inv -> {
                    counter.accumulate(1);
                    if (counter.get() <= configuration.getMaxFailures()+1) {
                        return List.of(mentionTask);
                    }
                    return List.of();
                });
        when(channelScrapper.scrap(CHANNEL_NAME, configuration.getChannelMessagesCount()))
                .thenReturn(null);
        when(notificator.approveRestoration(anyCollection()))
                .thenReturn(false);
        crossChatScrapper.scrap();
        verify(storage, never()).save(any());

        verify(mentionQueue, times(configuration.getMaxFailures() + 1))
                .update(isTaskWithStatus(mentionTask, TaskStatus.InvalidProcessed));

        verify(mentionQueue, times(configuration.getMaxFailures() + 2))
                .poll(configuration.getFetchingMentionTasksCount());

        verify(mentionQueue, never()).restore(anyList());
    }

    @Test
    public void testFilterBotNameFromQueue() {
        when(mentionQueue.poll(configuration.getFetchingMentionTasksCount()))
                .thenReturn(List.of(processedMentionTask("someBoT")), List.of());

        crossChatScrapper.scrap();
        verify(storage, never()).save(any());
        verify(mentionQueue, times(2)).poll(configuration.getFetchingMentionTasksCount());
    }

    @Test
    public void testFilterBotNameFromMessage() {
        List<Message> messages = List.of(message(MENTION1), message("Bot"));
        channel.setMessages(messages);
        crossChatScrapper.scrap();

        verify(storage, times(1)).save(channel);
        verify(mentionQueue, times(1)).update(List.of(initialMentionTask(MENTION1), processedMentionTask(CHANNEL_NAME)));
        verify(mentionQueue, times(2)).poll(configuration.getFetchingMentionTasksCount());
    }

    @Test
    public void testChatFilter() {
        when(filter.doFilter(channel))
                .thenReturn(false);
        crossChatScrapper.scrap();
        verify(storage, never()).save(any());
        verify(mentionQueue, times(1)).update(isTaskWithStatus(mentionTask, TaskStatus.Filtered));
        verify(mentionQueue, times(2)).poll(configuration.getFetchingMentionTasksCount());
    }

    @Test
    public void testMultiThreading() {
        when(mentionQueue.poll(configuration.getFetchingMentionTasksCount()))
                .thenReturn(List.of(mentionTask));
        when(channelScrapper.scrap(CHANNEL_NAME, configuration.getChannelMessagesCount()))
                .thenReturn(null);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        int threads = 2;
        var futures = IntStream.range(0,threads)
                .mapToObj(i -> pool.submit(() -> crossChatScrapper.scrap()))
                .collect(Collectors.toList());
        while (futures.stream().anyMatch(Predicate.not(Future::isDone))) { /*waiting*/ }

        verify(storage, never()).save(any());

        var shortListInvocations = new LongAccumulator(Long::sum, 0);
        var longListInvocations = new LongAccumulator(Long::sum, 0);

        verify(mentionQueue, times(2)).restore(argThat(list -> {
            if (list.equals(List.of(mentionTask))){
                shortListInvocations.accumulate(1);
                return true;
            }
            longListInvocations.accumulate(1);
            System.out.println(list.size());
            assertTrue(configuration.getFetchingMentionTasksCount() <= list.size() &&  list.size() <= (configuration.getFetchingMentionTasksCount() + threads));
            list.forEach(e -> assertEquals(e, mentionTask));
            return true;
        }));
        assertEquals(2, shortListInvocations.get());//times(2) but shortListInvocations = 2 - bug of mockito?
        assertEquals(2, longListInvocations.get());//times(2) but longListInvocations = 2 - bug of mockito?
        verify(notificator).approveRestoration(anyCollection());
    }

    @Nonnull
    private Message message(String mention) {
        return Message.builder()
                .channel(channel)
                .mentions(Set.of(Mention.builder().channelName(mention).build()))
                .build();
    }

}