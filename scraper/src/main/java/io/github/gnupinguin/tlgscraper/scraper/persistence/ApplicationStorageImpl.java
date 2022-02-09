package io.github.gnupinguin.tlgscraper.scraper.persistence;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Message;
import io.github.gnupinguin.tlgscraper.scraper.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final ForwardingRepository forwardingRepository;
    private final ReplyingRepository replyingRepository;
    private final ChannelRepository channelRepository;

    @Override
    public void save(@Nonnull Channel channel) {
        if (isChannelProcessed(channel)) {
            log.info("Channel already scrapped: {}", channel);
            return;
        }
        channelRepository.save(channel);
        messageRepository.saveAll(channel.getMessages());

        var mentions = unionEntities(channel.getMessages(), Message::getMentions);
        var hashtags = unionEntities(channel.getMessages(), Message::getHashTags);
        var links = unionEntities(channel.getMessages(), Message::getLinks);

        log.info("Extracted entities for channel '{}': mentions: {}, links: {}, hashTags: {}", channel.getName(), mentions.size(), links.size(), hashtags.size());

        mentionRepository.saveAll(mentions);
        hashTagRepository.saveAll(hashtags);
        linkRepository.saveAll(links);
        forwardingRepository.saveAll(unionSingleEntities(channel.getMessages(), Message::getForwarding));
        replyingRepository.saveAll(unionSingleEntities(channel.getMessages(), Message::getReplying));
    }

    private boolean isChannelProcessed(@Nonnull Channel channel) {
        return channelRepository.getChannelByName(channel.getName()) != null;
    }

    private <T> List<T> unionEntities(List<Message> messages, Function<Message, Collection<T>> mapper) {
        return messages.stream()
                .map(mapper)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private <T> List<T> unionSingleEntities(List<Message> messages, Function<Message, T> mapper) {
        return messages.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    //TODO can be replaced to single query
    public List<String> restoreUnprocessedEntities(List<String> blockedMentions) {
       var chats = channelRepository.getChannelsByNameIsIn(blockedMentions);

        var foundChatNames = getNames(chats);

        var notFound = blockedMentions.stream()
                .filter(not(foundChatNames::contains))
                .collect(Collectors.toList());

        var halfProcessedChats = chats.stream()
                .filter(channel -> messageRepository.getMessagesByChannel(channel).isEmpty())
                .collect(Collectors.toList());

        channelRepository.deleteAll(halfProcessedChats);

        var halfProcessedChatNames = getNames(halfProcessedChats);
        return Stream.concat(notFound.stream(), halfProcessedChatNames.stream())
                .collect(Collectors.toList());
    }

    @Nonnull
    private List<String> getNames(List<Channel> chats) {
        return chats.stream()
                .map(Channel::getName)
                .collect(Collectors.toList());
    }

}
