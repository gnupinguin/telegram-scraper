package io.github.gnupinguin.tlgscraper.scraper.persistence;

import io.github.gnupinguin.tlgscraper.db.repository.*;
import io.github.gnupinguin.tlgscraper.model.db.*;
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
    private final ChatRepository chatRepository;

    @Override
    public void save(@Nonnull Chat chat) {
        if (!isChatPresent(chat)) {
            log.info("Channel already scrapped: {}", chat);
            return;
        }
        chatRepository.save(chat);
        messageRepository.save(chat.getMessages());

        List<Mention> mentions = unionEntities(chat.getMessages(), Message::getMentions);
        List<HashTag> hashtags = unionEntities(chat.getMessages(), Message::getHashTags);
        List<Link> links = unionEntities(chat.getMessages(), Message::getLinks);

        log.info("Extracted entities for channel '{}': mentions: {}, links: {}, hashTags: {}", chat.getName(), mentions.size(), links.size(), hashtags.size());

        mentionRepository.save(mentions);
        hashTagRepository.save(hashtags);
        linkRepository.save(links);
        forwardingRepository.save(unionSingleEntities(chat.getMessages(), Message::getForwarding));
        replyingRepository.save(unionSingleEntities(chat.getMessages(), Message::getReplying));
    }

    private boolean isChatPresent(@Nonnull Chat chat) {
        return chatRepository.getChatsByNames(List.of(chat.getName())).isEmpty();
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
