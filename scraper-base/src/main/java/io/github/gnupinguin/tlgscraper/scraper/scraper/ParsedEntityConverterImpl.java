package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.*;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParsedEntityConverterImpl implements ParsedEntityConverter {

    private static final long CHANNEL_MESSAGE_ID = -1L;

    @Nonnull
    @Override
    public Channel convert(@Nonnull ParsedEntity<WebChannel> parsedChannel,
                           @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        Channel channel = getChat(parsedChannel);
        Message channelInfoMessage = channelInfoMessage(channel, parsedChannel);
        updateMessages(channel, channelInfoMessage, parsedMessages);
        return channel;
    }

    @Override
    public void update(@Nonnull Channel channel, @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        updateMessages(channel, null, parsedMessages);
    }

    private Channel getChat(@Nonnull ParsedEntity<WebChannel> parsedChannel) {
        WebChannel webChannel = parsedChannel.entity();
        return Channel.builder()
                .name(webChannel.name())
                .title(webChannel.title())
                .description(webChannel.description())
                .members(webChannel.users())
                .build();
    }

    @Nonnull
    private Message channelInfoMessage(@Nonnull Channel channel,
                                       @Nonnull ParsedEntity<WebChannel> parsedEntity) {
        Timestamp timestamp = timestamp(parsedEntity.loadDate());
        return baseMessage(channel, parsedEntity)
                .id(CHANNEL_MESSAGE_ID)
                .type(MessageType.ChannelInfo)
                .publishDate(timestamp)
                .viewCount(0)
                .build();
    }

    private void updateMessages(@Nonnull Channel channel,
                                @Nullable Message chatMessage,
                                @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        List<Message> messages = getMessagesStream(channel, chatMessage, parsedMessages)
                .peek(message -> message.getMentions().forEach(mention -> mention.setMessage(message)))
                .peek(message -> message.getHashTags().forEach(tag -> tag.setMessage(message)))
                .peek(message -> message.getLinks().forEach(link -> link.setMessage(message)))
                .peek(this::updateForwarding)
                .peek(this::updateReplying)
                .collect(Collectors.toList());
        channel.setMessages(messages);
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
    private Stream<Message> getMessagesStream(@Nonnull Channel channel,
                                              @Nullable Message chatMessage,
                                              @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        return Stream.concat(
                Stream.of(chatMessage),
                parsedMessages.stream().map(pm -> convertMessage(channel, pm)))
                .filter(Objects::nonNull);
    }

    @Nonnull
    private Message convertMessage(Channel channel, ParsedEntity<WebMessage> parsedMessage) {
        WebMessage message = parsedMessage.entity();
        return baseMessage(channel, parsedMessage)
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
    private Message.MessageBuilder baseMessage(@Nonnull Channel channel,
                                               @Nonnull ParsedEntity<?> parsedEntity) {
        return Message.builder()
                .channel(channel)
                .loadDate(timestamp(parsedEntity.loadDate()))
                .hashTags(getHashTags(parsedEntity))
                .mentions(getMentions(parsedEntity))
                .links(getLinks(parsedEntity));
    }

    @Nonnull
    private Set<HashTag> getHashTags(@Nonnull ParsedEntity<?> parsedEntity) {
        return parsedEntity.hashTags().stream()
                .map(tag -> HashTag.builder().tag(tag).build())
                .collect(Collectors.toSet());
    }

    @Nonnull
    private Set<Mention> getMentions(ParsedEntity<?> parsedEntity) {
        return parsedEntity.mentions().stream()
                .map(mention -> Mention.builder().chatName(mention).build())
                .collect(Collectors.toSet());
    }

    @Nonnull
    private Set<Link> getLinks(ParsedEntity<?> parsedEntity) {
        return parsedEntity.links().stream()
                .map(link -> Link.builder().url(link).build())
                .collect(Collectors.toSet());
    }

}
