package org.textindexer.extractor.persistence;

import io.github.gnupinguin.tlgscraper.db.repository.*;
import io.github.gnupinguin.tlgscraper.model.db.*;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ApplicationStorageImpl implements ApplicationStorage {

    private final MessageRepository messageRepository;
    private final MentionRepository mentionRepository;
    private final LinkRepository linkRepository;
    private final HashTagRepository hashTagRepository;
    private final ChatRepository chatRepository;

    @Override
    public void save(@Nonnull Chat channel, @Nonnull List<MessageInfo> messages) {
        if (chatRepository.get(channel.getId()) != null) {
            log.info("Channel already scrapped: {}", channel);
            return;
        }
        chatRepository.save(channel);

        List<Message> mappedMessages = messages.stream()
                .map(this::mapMessage)
                .collect(Collectors.toList());

        messageRepository.save(mappedMessages);

        List<Mention> mentions = new ArrayList<>(8);
        List<HashTag> hashtags = new ArrayList<>(8);
        List<Link> links = new ArrayList<>(8);

        IntStream.range(0, messages.size())
                .forEach(i -> {
                    var internalMessageId = mappedMessages.get(i).getInternalId();
                    var message = messages.get(i);

                    mentions.addAll(mapMentions(internalMessageId, message));
                    hashtags.addAll(mapHashTags(internalMessageId, message));
                    links.addAll(mapLinks(internalMessageId, message));
                });

        log.info("Extracted entities for channel '{}': mentions: {}, links: {}, hashTags: {}", channel.getName(), mentions.size(), links.size(), hashtags.size());
        mentionRepository.save(mentions);
        hashTagRepository.save(hashtags);
        linkRepository.save(links);
    }

    @Nonnull
    private Message mapMessage(MessageInfo message) {
        return new Message(null, message.getChatId(), message.getId(),
                message.getReplyToMessageId(),
                message.getForwardedFromChatId(), message.getForwardedFromMessageId(),
                message.getType().getTypeId(), message.getTextContent(),
                sqlDate(message.getPublishDate()), sqlDate(message.getLoadDate()),
                message.getViewCount());
    }

    private java.sql.Timestamp sqlDate(Date date) {
        return new java.sql.Timestamp(date.getTime());
    }

    @Nonnull
    private List<Mention> mapMentions(@Nonnull Long internalMessageId, @Nonnull MessageInfo message) {
        return message.getMentions().stream()
                .map(chatName -> new Mention(null, internalMessageId, chatName))
                .collect(Collectors.toList());
    }

    @Nonnull
    private List<Link> mapLinks(@Nonnull Long internalMessageId, @Nonnull MessageInfo message) {
        return message.getLinks().stream()
                .map(link -> new Link(null, internalMessageId, link))
                .collect(Collectors.toList());
    }

    @Nonnull
    private List<HashTag> mapHashTags(@Nonnull Long internalMessageId, @Nonnull MessageInfo message) {
        return message.getHashTags().stream()
                .map(hashTag -> new HashTag(null, internalMessageId, hashTag))
                .collect(Collectors.toList());
    }

    @Override
    //TODO can be replaced to single query
    public List<String> restore(List<String> blockedMentions) {
        List<Chat> chats = chatRepository.getChatsByNames(blockedMentions);

        var foundChatNames = getNames(chats);

        var notFound = blockedMentions.stream()
                .filter(not(foundChatNames::contains))
                .collect(Collectors.toList());

        var halfProcessedChats = chats.stream()
                .filter(chat -> messageRepository.getForChat(chat.getId()).isEmpty())
                .collect(Collectors.toList());

        chatRepository.delete(halfProcessedChats.stream()
                .map(Chat::getId)
                .collect(Collectors.toList()));

        var halfProcessedChatNames = getNames(halfProcessedChats);
        return Stream.concat(notFound.stream(), halfProcessedChatNames.stream())
                .collect(Collectors.toList());
    }

    @Nonnull
    private List<String> getNames(List<Chat> chats) {
        return chats.stream()
                .map(Chat::getName)
                .collect(Collectors.toList());
    }
}
