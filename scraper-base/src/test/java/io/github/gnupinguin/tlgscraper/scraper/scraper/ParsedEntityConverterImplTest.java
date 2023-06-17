package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.*;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@ExtendWith(MockitoExtension.class)
public class ParsedEntityConverterImplTest {

    private static final String CHANNEL = "channel";
    private static final Date DATE = new Date();

    @InjectMocks
    ParsedEntityConverterImpl converter;

    @Test
    void test() {
        var parsedChannel = parsedChannel();
        var parsedMessage = parsedMessage();
        var chat = converter.convert(parsedChannel, List.of(parsedMessage));

        assertEquals(parsedChannel.entity().name(), chat.getName());
        assertEquals(parsedChannel.entity().title(), chat.getTitle());
        assertEquals(parsedChannel.entity().description(), chat.getDescription());
        assertEquals((Integer)parsedChannel.entity().users(), chat.getMembers());

        List<Message> messages = chat.getMessages();
        assertEquals(2, messages.size());
        assertEquals(chat, messages.get(0).getChannel());

        assertEquals(-1L, messages.get(0).getId());
        assertEquals(MessageType.ChannelInfo, messages.get(0).getType());
        assertEquals(0, messages.get(0).getViewCount());
        assertEquals(timestamp(), messages.get(0).getLoadDate());
        assertEquals(timestamp(), messages.get(0).getPublishDate());
        assertNull(messages.get(0).getTextContent());
        assertNull(messages.get(0).getForwarding());
        assertNull(messages.get(0).getReplying());
        assertNull(messages.get(0).getInternalId());
        checkMention(parsedChannel, messages.get(0));
        checkHashTag(parsedChannel, messages.get(0));
        checkLink(parsedChannel, messages.get(0));

        assertNull(messages.get(1).getInternalId());
        assertEquals(parsedMessage.entity().getId(), messages.get(1).getId());
        assertEquals(MessageType.Text, messages.get(1).getType());
        assertEquals(parsedMessage.entity().getViewCount(), messages.get(1).getViewCount());
        assertEquals(timestamp(), messages.get(1).getLoadDate());
        assertEquals(timestamp(), messages.get(1).getPublishDate());
        assertEquals(parsedMessage.entity().getTextContent(), messages.get(1).getTextContent());
        checkMention(parsedMessage, messages.get(1));
        checkHashTag(parsedMessage, messages.get(1));
        checkLink(parsedMessage, messages.get(1));
        checkForwarding(parsedMessage, messages.get(1));
        checkReplying(parsedMessage, messages.get(1));
    }

    @Test
    void testEmptyReplying() {
        var parsedChannel = parsedChannel();
        var parsedMessage = parsedMessage();
        parsedMessage.entity().setReplyToMessageId(null);

        var channel = converter.convert(parsedChannel, List.of(parsedMessage));

        List<Message> messages = channel.getMessages();
        assertNull(messages.get(1).getReplying());
    }

    @Test
    void testEmptyForwarding() {
        var parsedChannel = parsedChannel();
        var parsedMessage = parsedMessage();
        parsedMessage.entity().setForwardedFromChannel(null);

        var channel = converter.convert(parsedChannel, List.of(parsedMessage));

        List<Message> messages = channel.getMessages();
        assertNull(messages.get(1).getForwarding());
    }

    private Timestamp timestamp() {
        return Timestamp.from(DATE.toInstant());
    }

    private void checkMention(ParsedEntity<?> parsedEntity, Message message) {
        Mention mention = anyElement(message.getMentions());
        assertEquals(message, mention.getMessage());
        assertEquals(anyElement(parsedEntity.mentions()), mention.getChatName());
        assertNull(mention.getId());
    }

    private void checkHashTag(ParsedEntity<?> parsedEntity, Message message) {
        HashTag hashTag = anyElement(message.getHashTags());
        assertEquals(message, hashTag.getMessage());
        assertEquals(anyElement(parsedEntity.hashTags()), hashTag.getTag());
        assertNull(hashTag.getId());
    }

    private void checkLink(ParsedEntity<?> parsedEntity, Message message) {
        Link link = anyElement(message.getLinks());
        assertEquals(message, link.getMessage());
        assertEquals(anyElement(parsedEntity.links()), link.getUrl());
        assertNull(link.getId());
    }

    private void checkForwarding(ParsedEntity<WebMessage> parsedEntity, Message message) {
        Forwarding forwarding = message.getForwarding();
        assertEquals(message, forwarding.getMessage());
        assertEquals(parsedEntity.entity().getForwardedFromChannel(), forwarding.getForwardedFromChannel());
        assertEquals(parsedEntity.entity().getForwardedFromMessageId(), forwarding.getForwardedFromMessageId());
    }

    private void checkReplying(ParsedEntity<WebMessage> parsedEntity, Message message) {
        Replying replying = message.getReplying();
        assertEquals(message, replying.getMessage());
        assertEquals(parsedEntity.entity().getReplyToMessageId(), replying.getReplyToMessageId());
    }

    private <T> T anyElement(Collection<T> entities) {
        return entities.iterator().next();
    }

    private ParsedEntity<WebChannel> parsedChannel() {
        return new ParsedEntity<>(getChannel(),
                DATE,
                Set.of("chatMention"),
                Set.of("chatLink"),
                Set.of("chatHashtag"));
    }

    private ParsedEntity<WebMessage> parsedMessage() {
        return new ParsedEntity<>(getWebMessage(),
                DATE,
                Set.of("messageMention"),
                Set.of("messageLink"),
                Set.of("messageHashtag"));
    }
    
    private WebChannel getChannel() {
        return new WebChannel(CHANNEL, "title", "description", 1);
    }

    private WebMessage getWebMessage() {
        return WebMessage.builder()
                .id(1)
                .forwardedFromChannel("forwarded")
                .forwardedFromMessageId(2L)
                .replyToMessageId(3L)
                .channel(CHANNEL)
                .publishDate(DATE)
                .viewCount(32)
                .textContent("text")
                .type(MessageType.Text)
                .build();
    }

}