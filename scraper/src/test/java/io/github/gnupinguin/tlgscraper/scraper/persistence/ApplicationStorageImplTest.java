package io.github.gnupinguin.tlgscraper.scraper.persistence;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.*;
import io.github.gnupinguin.tlgscraper.scraper.persistence.repository.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationStorageImplTest {

    private static final String URL = "https://u.rl";

    private static final String TEXT = "text";

    public static final Date DATE = new Date();

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MentionRepository mentionRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private HashTagRepository hashTagRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ForwardingRepository forwardingRepository;

    @Mock
    private ReplyingRepository replyingRepository;

    @InjectMocks
    private ApplicationStorageImpl storage;

    @Test
    public void testSave() {
        Channel channel = getChannel();
        Message message1 = baseMessage(channel, 1);
        Message message2 = baseMessage(channel,2);
        channel.setMessages(List.of(message1, message2));

        storage.save(channel);

        verify(channelRepository, times(1)).save(channel);
        verify(messageRepository, times(1)).saveAll(List.of(message1, message2));

        verify(mentionRepository, times(1)).saveAll(List.of(
                message1.getMentions().toArray(new Mention[0])[0],
                message2.getMentions().toArray(new Mention[0])[0]
        ));
        verify(linkRepository, times(1)).saveAll(List.of(
                message1.getLinks().toArray(new Link[0])[0],
                message2.getLinks().toArray(new Link[0])[0]
        ));
        verify(hashTagRepository, times(1)).saveAll(List.of(
                message1.getHashTags().toArray(new HashTag[0])[0],
                message2.getHashTags().toArray(new HashTag[0])[0]
        ));
        verify(forwardingRepository, times(1)).saveAll(List.of(
                message1.getForwarding(),
                message2.getForwarding()
        ));
        verify(replyingRepository, times(1)).saveAll(List.of(
                message1.getReplying(),
                message2.getReplying()
        ));
    }

    private Channel getChannel() {
        return Channel.builder()
                .name("chat")
                .id(1L)
                .messages(List.of())
                .build();
    }

    @Test
    public void testAlreadyProcessedChannel() {
        Channel channel = getChannel();
        when(channelRepository.getChannelByName(channel.getName()))
                .thenReturn(channel);
        storage.save(channel);
        verifyNoMoreInteractions(
                messageRepository,
                mentionRepository,
                linkRepository,
                hashTagRepository,
                forwardingRepository,
                replyingRepository);
    }

    private Message baseMessage(Channel chat, long id) {
        var message = Message.builder()
                .channel(chat)
                .id(id)
                .links(Set.of(Link.builder()
                        .url(URL + id)
                        .build()))
                .mentions(Set.of(Mention.builder()
                        .channelName("mention" + id)
                        .build()))
                .hashTags(Set.of(HashTag.builder()
                        .tag("tag" + id)
                        .build()))
                .forwarding(Forwarding.builder()
                        .forwardedFromMessageId(id + 1)
                        .forwardedFromChannel("channel" + id)
                        .build())
                .replying(Replying.builder()
                        .replyToMessageId(id + 1)
                        .build())
                .textContent(TEXT)
                .loadDate(DATE)
                .publishDate(DATE)
                .type(MessageType.Text)
                .build();

        message.getLinks().forEach(l -> l.setMessage(message));
        message.getMentions().forEach(m -> m.setMessage(message));
        message.getHashTags().forEach(h -> h.setMessage(message));

        message.getForwarding().setMessage(message);
        message.getReplying().setMessage(message);

        return message;
    }

}