package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import io.github.gnupinguin.tlgscraper.db.queue.mention.BufferedMentionTaskQueue;
import io.github.gnupinguin.tlgscraper.db.queue.mention.MentionTask;
import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.db.Mention;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import io.github.gnupinguin.tlgscraper.scraper.notification.Notificator;
import io.github.gnupinguin.tlgscraper.scraper.persistence.ApplicationStorage;
import io.github.gnupinguin.tlgscraper.scraper.scraper.filter.ChatFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShortCrossChatScraperTest {

    private static final String CHANNEL_NAME = "channel";

    public static final String MENTION1 = "channel1";
    public static final String MENTION2 = "channel2";

    @Mock
    private ChatScrapper chatScrapper;

    @Mock
    private BufferedMentionTaskQueue mentionQueue;

    @Mock
    private ApplicationStorage storage;

    @Mock
    private ChatFilter filter;

    @Mock
    private Notificator notificator;

    @Mock
    private ScraperConfiguration configuration;

    @InjectMocks
    private ShortCrossChatScraper crossChatScrapper;

    private Chat chat;

    private MentionTask mentionTask = mentionTask(CHANNEL_NAME);

    @Before
    public void setUp() {
        chat = Chat.builder().name(CHANNEL_NAME).build();
        List<Message> messages= List.of(message(MENTION1), message(MENTION2));
        chat.setMessages(messages);

        when(chatScrapper.scrap(CHANNEL_NAME, 300))
                .thenReturn(chat);

        when(mentionQueue.poll())
                .thenReturn(mentionTask, new MentionTask[]{null});

        when(filter.doFilter(chat))
                .thenReturn(true);
        when(notificator.approveRestoration(anyCollection()))
                .thenReturn(true);
        when(configuration.getMaxFailures())
                .thenReturn(20);
        when(configuration.getMessagesCount())
                .thenReturn(300);
    }

    private MentionTask mentionTask(String name) {
        return new MentionTask(TaskStatus.SuccessfullyProcessed, name);
    }

    private MentionTask initialMentionTask(String name) {
        return new MentionTask(TaskStatus.Initial, name);
    }

    @Test
    public void testScrapFromQueue() {
        crossChatScrapper.scrap();

        verify(storage, times(1)).save(chat);
        verify(mentionQueue, times(1))
                .add(List.of(initialMentionTask(MENTION1), initialMentionTask(MENTION2)));
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testChatNotFound() {
        when(chatScrapper.scrap(CHANNEL_NAME, 300))
                .thenReturn(null);
        crossChatScrapper.scrap();
        verify(storage, never()).save(any());
        verify(mentionQueue, times(1)).markInvalid(mentionTask);
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testStopAfter20Failures() {
        when(mentionQueue.poll())
                .thenReturn(mentionTask);
        when(chatScrapper.scrap(CHANNEL_NAME, 300))
                .thenReturn(null);
        crossChatScrapper.scrap();
        verify(storage, never()).save(any());

        //21 with current chat
        verify(mentionQueue, times(20)).markInvalid(mentionTask);
        verify(mentionQueue, times(21)).poll();
        verify(mentionQueue, times(1)).restore(Collections.nCopies(21, mentionTask));
    }

    @Test
    public void testContinueAfter20FailuresButNotApproved() {
        ArrayList<MentionTask> channels = new ArrayList<>(Collections.nCopies(20, mentionTask));
        channels.add(null);
        when(mentionQueue.poll())
                .thenReturn(mentionTask, channels.toArray(new MentionTask[0]));
        when(chatScrapper.scrap(CHANNEL_NAME, 300))
                .thenReturn(null);
        when(notificator.approveRestoration(anyCollection()))
                .thenReturn(false);
        crossChatScrapper.scrap();
        verify(storage, never()).save(any());
        verify(mentionQueue, times(21)).markInvalid(mentionTask);
        verify(mentionQueue, times(22)).poll();
        verify(mentionQueue, never()).restore(anyList());
    }

    @Test
    public void testFilterBotNameFromQueue() {
        when(mentionQueue.poll())
                .thenReturn(mentionTask("someBoT"), new MentionTask[]{null});

        crossChatScrapper.scrap();
        verify(storage, never()).save(any());
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testFilterBotNameFromMessage() {
        List<Message> messages = List.of(message(MENTION1), message("Bot"));
        chat.setMessages(messages);
        crossChatScrapper.scrap();

        verify(storage, times(1)).save(chat);
        verify(mentionQueue, times(1)).add(List.of(initialMentionTask(MENTION1)));
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testChatFilter() {
        when(filter.doFilter(chat))
                .thenReturn(false);
        crossChatScrapper.scrap();
        verify(storage, never()).save(any());
        verify(mentionQueue, times(1)).markFiltered(mentionTask);
        verify(mentionQueue, times(2)).poll();
    }

    @Test
    public void testMultiThreading() throws Exception {
        when(mentionQueue.poll())
                .thenReturn(mentionTask);
        when(chatScrapper.scrap(CHANNEL_NAME, 300))
                .thenReturn(null);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        int threads = 2;
        IntStream.range(0,threads).forEach(i -> pool.submit(() -> crossChatScrapper.scrap()));
        pool.awaitTermination(3, TimeUnit.SECONDS);

        verify(storage, never()).save(any());

        AtomicInteger shortListInvocations = new AtomicInteger(0);
        AtomicInteger longListInvocations = new AtomicInteger(0);

        verify(mentionQueue, times(2)).restore(argThat(list -> {
            if (list.equals(List.of(mentionTask))){
                shortListInvocations.incrementAndGet();
                return true;
            }
            longListInvocations.incrementAndGet();
            System.out.println(list.size());
            assertTrue(20 <= list.size() &&  list.size() <= (20 + threads));
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
                .channel(chat)
                .mentions(Set.of(Mention.builder().chatName(mention).build()))
                .build();
    }

}