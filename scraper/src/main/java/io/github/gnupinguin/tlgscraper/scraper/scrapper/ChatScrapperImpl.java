package io.github.gnupinguin.tlgscraper.scraper.scrapper;

import io.github.gnupinguin.tlgscraper.model.db.*;
import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.TelegramWebClient;
import io.github.gnupinguin.tlgscraper.scraper.web.ParsedEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@RequiredArgsConstructor
public class ChatScrapperImpl implements ChatScrapper {

    private static final long CHANNEL_MESSAGE_ID = -1L;

    private final TelegramWebClient webClient;

    @Nullable
    @Override
    public Chat scrap(String channel, int count) {
        ParsedEntity<Channel> parsedChannel = webClient.searchChannel(channel);
        if (parsedChannel != null) {
            List<ParsedEntity<WebMessage>> parsedMessages = webClient.getLastMessages(channel, count);
            Chat chat = getChat(parsedChannel);
            Message channelInfoMessage = channelInfoMessage(chat, parsedChannel);
            updateMessages(chat, channelInfoMessage, parsedMessages);
            return chat;
        }
        return null;
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
        return baseMessage(chat, parsedEntity)
                .id(CHANNEL_MESSAGE_ID)
                .build();
    }

    private void updateMessages(@Nonnull Chat chat,
                                @Nonnull Message chatMessage,
                                @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        List<Message> messages = getMessagesStream(chat, chatMessage, parsedMessages)
                .peek(message -> message.getMentions().forEach(mention -> mention.setMessage(message)))
                .peek(message -> message.getHashTags().forEach(tag -> tag.setMessage(message)))
                .peek(message -> message.getLinks().forEach(link -> link.setMessage(message)))
                .collect(Collectors.toList());
        chat.setMessages(messages);
    }

    @Nonnull
    private Stream<Message> getMessagesStream(@Nonnull Chat chat,
                                              @Nonnull Message chatMessage,
                                              @Nonnull List<ParsedEntity<WebMessage>> parsedMessages) {
        return Stream.concat(
                Stream.of(chatMessage),
                parsedMessages.stream().map(pm -> convertMessage(chat, pm))
        );
    }

    @Nonnull
    private Message convertMessage(Chat chat, ParsedEntity<WebMessage> parsedMessage) {
        WebMessage message = parsedMessage.getEntity();
        return baseMessage(chat, parsedMessage)
                .id(message.getId())
                .loadDate(message.getLoadDate())
                .publishDate(message.getPublishDate())
                .type(message.getType())
                .textContent(message.getTextContent())
                .viewCount(message.getViewCount())
                .build();
    }

    @Nonnull
    private Message.MessageBuilder baseMessage(@Nonnull Chat chat,
                                               @Nonnull ParsedEntity<?> parsedEntity) {
        return Message.builder()
                .channel(chat)
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
