package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.*;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ParsedEntityConverterImpl implements ParsedEntityConverter {

    private static final long CHANNEL_MESSAGE_ID = -1L;

    @Nonnull
    @Override
    public Chat convert(@Nonnull ParsedEntity<Channel> parsedChannel,
                        @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        Chat chat = getChat(parsedChannel);
        Message channelInfoMessage = channelInfoMessage(chat, parsedChannel);
        updateMessages(chat, channelInfoMessage, parsedMessages);
        return chat;
    }

    @Override
    public void update(@Nonnull Chat chat, @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        updateMessages(chat, null, parsedMessages);
    }

    private Chat getChat(@Nonnull ParsedEntity<Channel> parsedChannel) {
        Channel channel = parsedChannel.getEntity();
        return Chat.builder()
                .name(channel.getName())
                .title(channel.getTitle())
                .description(channel.getDescription())
                .members(channel.getUsers())
                .build();
    }

    @Nonnull
    private Message channelInfoMessage(@Nonnull Chat chat,
                                       @Nonnull ParsedEntity<Channel> parsedEntity) {
        Timestamp timestamp = timestamp(parsedEntity.getLoadDate());
        return baseMessage(chat, parsedEntity)
                .id(CHANNEL_MESSAGE_ID)
                .type(MessageType.ChannelInfo)
                .publishDate(timestamp)
                .viewCount(0)
                .build();
    }

    private void updateMessages(@Nonnull Chat chat,
                                @Nullable Message chatMessage,
                                @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        List<Message> messages = getMessagesStream(chat, chatMessage, parsedMessages)
                .peek(message -> message.getMentions().forEach(mention -> mention.setMessage(message)))
                .peek(message -> message.getHashTags().forEach(tag -> tag.setMessage(message)))
                .peek(message -> message.getLinks().forEach(link -> link.setMessage(message)))
                .peek(this::updateForwarding)
                .peek(this::updateReplying)
                .collect(Collectors.toList());
        chat.setMessages(messages);
    }

    private void updateForwarding(Message message) {
        if (message.getForwarding() != null) {
            message.getForwarding().setMessage(message);
        }
    }

    private void updateReplying(Message message) {
        if (message.getReplying() != null) {
            message.getReplying().setMessage(message);
        }
    }

    @Nonnull
    private Stream<Message> getMessagesStream(@Nonnull Chat chat,
                                              @Nullable Message chatMessage,
                                              @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        return Stream.concat(
                Stream.of(chatMessage),
                parsedMessages.stream().map(pm -> convertMessage(chat, pm)))
                .filter(Objects::nonNull);
    }

    @Nonnull
    private Message convertMessage(Chat chat, ParsedEntity<WebMessage> parsedMessage) {
        WebMessage message = parsedMessage.getEntity();
        return baseMessage(chat, parsedMessage)
                .id(message.getId())
                .publishDate(timestamp(message.getPublishDate()))
                .type(message.getType())
                .textContent(message.getTextContent())
                .viewCount(message.getViewCount())
                .forwarding(getForwarding(message))
                .replying(getReplying(message))
                .build();
    }

    private Forwarding getForwarding(WebMessage message) {
        if (message.getForwardedFromChannel() != null) {
            return Forwarding.builder()
                    .forwardedFromChannel(message.getForwardedFromChannel())
                    .forwardedFromMessageId(message.getForwardedFromMessageId())
                    .build();
        }
        return null;
    }

    private Replying getReplying(WebMessage message) {
        if (message.getReplyToMessageId() != null) {
            return Replying.builder()
                    .replyToMessageId(message.getReplyToMessageId())
                    .build();
        }
        return null;
    }

    @Nonnull
    private Timestamp timestamp(Date loadDate) {
        return Timestamp.from(loadDate.toInstant());
    }

    @Nonnull
    private Message.MessageBuilder baseMessage(@Nonnull Chat chat,
                                               @Nonnull ParsedEntity<?> parsedEntity) {
        return Message.builder()
                .channel(chat)
                .loadDate(timestamp(parsedEntity.getLoadDate()))
                .hashTags(getHashTags(parsedEntity))
                .mentions(getMentions(parsedEntity))
                .links(getLinks(parsedEntity));
    }

    @Nonnull
    private Set<HashTag> getHashTags(@Nonnull ParsedEntity<?> parsedEntity) {
        return parsedEntity.getHashTags().stream()
                .map(tag -> HashTag.builder().tag(tag).build())
                .collect(Collectors.toSet());
    }

    @Nonnull
    private Set<Mention> getMentions(ParsedEntity<?> parsedEntity) {
        return parsedEntity.getMentions().stream()
                .map(mention -> Mention.builder().chatName(mention).build())
                .collect(Collectors.toSet());
    }

    @Nonnull
    private Set<Link> getLinks(ParsedEntity<?> parsedEntity) {
        return parsedEntity.getLinks().stream()
                .map(link -> Link.builder().url(link).build())
                .collect(Collectors.toSet());
    }

}
