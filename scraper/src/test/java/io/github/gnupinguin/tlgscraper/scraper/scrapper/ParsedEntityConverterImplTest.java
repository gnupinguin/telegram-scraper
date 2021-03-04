package io.github.gnupinguin.tlgscraper.scraper.scrapper;

import io.github.gnupinguin.tlgscraper.model.db.*;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ParsedEntityConverterImplTest {

    private static final String CHANNEL = "channel";
    private static final Date DATE = new Date();

    @InjectMocks
    private ParsedEntityConverterImpl converter;

    @Test
    public void test() {
        var parsedChannel = parsedChannel();
        var parsedMessage = parsedMessage();
        Chat chat = converter.convert(parsedChannel, List.of(parsedMessage));

        assertEquals(parsedChannel.getEntity().getName(), chat.getName());
        assertEquals(parsedChannel.getEntity().getTitle(), chat.getTitle());
        assertEquals(parsedChannel.getEntity().getDescription(), chat.getDescription());
        assertEquals((Integer)parsedChannel.getEntity().getUsers(), chat.getMembers());

        List<Message> messages = chat.getMessages();
        assertEquals(2, messages.size());
        assertEquals(chat, messages.get(0).getChannel());

        assertEquals(-1L, messages.get(0).getId());
        assertEquals(MessageType.ChannelInfo, messages.get(0).getType());
        assertEquals(0, messages.get(0).getViewCount());
        assertNull(messages.get(0).getTextContent());
        assertNull(messages.get(0).getForwarding());
        assertNull(messages.get(0).getReplying());
        assertNull(messages.get(0).getInternalId());
        assertTrue(messages.get(0).getLoadDate() instanceof Timestamp);
        assertTrue(messages.get(0).getPublishDate() instanceof Timestamp);
        checkMention(parsedChannel, messages.get(0));
        checkHashTag(parsedChannel, messages.get(0));
        checkLink(parsedChannel, messages.get(0));

        assertNull(messages.get(1).getInternalId());
        assertEquals(parsedMessage.getEntity().getId(), messages.get(1).getId());
        assertEquals(MessageType.Text, messages.get(1).getType());
        assertEquals(parsedMessage.getEntity().getViewCount(), messages.get(1).getViewCount());
        assertEquals(timestamp(), messages.get(1).getLoadDate());
        assertEquals(timestamp(), messages.get(1).getPublishDate());
        assertEquals(parsedMessage.getEntity().getTextContent(), messages.get(1).getTextContent());
        checkMention(parsedMessage, messages.get(1));
        checkHashTag(parsedMessage, messages.get(1));
        checkLink(parsedMessage, messages.get(1));
        checkForwarding(parsedMessage, messages.get(1));
        checkReplying(parsedMessage, messages.get(1));
    }

    @Test
    public void testEmptyReplying() {
        var parsedChannel = parsedChannel();
        var parsedMessage = parsedMessage();
        parsedMessage.getEntity().setReplyToMessageId(null);

        Chat chat = converter.convert(parsedChannel, List.of(parsedMessage));

        List<Message> messages = chat.getMessages();
        assertNull(messages.get(1).getReplying());
    }

    @Test
    public void testEmptyForwarding() {
        var parsedChannel = parsedChannel();
        var parsedMessage = parsedMessage();
        parsedMessage.getEntity().setForwardedFromChannel(null);

        Chat chat = converter.convert(parsedChannel, List.of(parsedMessage));

        List<Message> messages = chat.getMessages();
        assertNull(messages.get(1).getForwarding());
    }

    @Nonnull
    private Timestamp timestamp() {
        return Timestamp.from(DATE.toInstant());
    }

    private void checkMention(ParsedEntity<?> parsedEntity, Message message) {
        Mention mention = extract(message.getMentions());
        assertEquals(message, mention.getMessage());
        assertEquals(extract(parsedEntity.getMentions()), mention.getChatName());
        assertNull(mention.getId());
    }

    private void checkHashTag(ParsedEntity<?> parsedEntity, Message message) {
        HashTag hashTag = extract(message.getHashTags());
        assertEquals(message, hashTag.getMessage());
        assertEquals(extract(parsedEntity.getHashTags()), hashTag.getTag());
        assertNull(hashTag.getId());
    }

    private void checkLink(ParsedEntity<?> parsedEntity, Message message) {
        Link link = extract(message.getLinks());
        assertEquals(message, link.getMessage());
        assertEquals(extract(parsedEntity.getLinks()), link.getUrl());
        assertNull(link.getId());
    }

    private void checkForwarding(ParsedEntity<WebMessage> parsedEntity, Message message) {
        Forwarding forwarding = message.getForwarding();
        assertEquals(message, forwarding.getMessage());
        assertEquals(parsedEntity.getEntity().getForwardedFromChannel(), forwarding.getForwardedFromChannel());
        assertEquals(parsedEntity.getEntity().getForwardedFromMessageId(), forwarding.getForwardedFromMessageId());
    }

    private void checkReplying(ParsedEntity<WebMessage> parsedEntity, Message message) {
        Replying replying = message.getReplying();
        assertEquals(message, replying.getMessage());
        assertEquals(parsedEntity.getEntity().getReplyToMessageId(), replying.getReplyToMessageId());
    }

    private <T> T extract(Collection<T> entities) {
        return (T)entities.toArray()[0];
    }

    @Nonnull
    private ParsedEntity<Channel> parsedChannel() {
        return new ParsedEntity<>(getChannel(),
                Set.of("chatMention"),
                Set.of("chatLink"),
                Set.of("chatHashtag"));
    }

    @Nonnull
    private ParsedEntity<WebMessage> parsedMessage() {
        return new ParsedEntity<>(getWebMessage(),
                Set.of("messageMention"),
                Set.of("messageLink"),
                Set.of("messageHashtag"));
    }

    @Nonnull
    private Channel getChannel() {
        return new Channel(CHANNEL, "title", "description", 1);
    }

    private WebMessage getWebMessage() {
        return WebMessage.builder()
                .id(1)
                .forwardedFromChannel("forwarded")
                .forwardedFromMessageId(2L)
                .replyToMessageId(3L)
                .channel(CHANNEL)
                .publishDate(DATE)
                .loadDate(DATE)
                .viewCount(32)
                .textContent("text")
                .type(MessageType.Text)
                .build();
    }

}