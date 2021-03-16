package io.github.gnupinguin.tlgscraper.scraper.telegram;

import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ProxiedHttpClient;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.TelegramHtmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProxiedTelegramWebClient implements TelegramWebClient {

    private static final Comparator<ParsedEntity<WebMessage>> COMPARATOR = Comparator.comparing(p -> p.getEntity().getId(), Comparator.reverseOrder());

    private final TelegramHtmlParser parser;
    private final ProxiedHttpClient client;
    private final TelegramRequestLimiter limiter;

    @Override
    @Nullable
    public ParsedEntity<Channel> searchChannel(@Nonnull String channel) {
        return request("https://t.me/" + channel)
                .map(parser::parseChannel)
                .orElse(null);
    }

    @Nonnull
    @Override
    public List<ParsedEntity<WebMessage>> getLastMessages(@Nonnull String channel, int count) {
        TreeSet<ParsedEntity<WebMessage>> result = new TreeSet<>(COMPARATOR);
        result.addAll(requestMessages(channel));
        if (result.size() < count) {
            result.addAll(getMessagesBefore(channel, getId(result.last()), count - result.size()));
        }
        return new ArrayList<>(result);
    }

    private long getId(@Nonnull ParsedEntity<WebMessage> parsedEntity) {
        return parsedEntity.getEntity().getId();
    }

    @Nonnull
    @Override
    public List<ParsedEntity<WebMessage>> getMessagesBefore(@Nonnull String channel, long beforeId, int count) {
        Set<ParsedEntity<WebMessage>> result = new TreeSet<>(COMPARATOR);
        List<ParsedEntity<WebMessage>> entities = requestMessages(channel, beforeId);
        while (!entities.isEmpty() && result.size() < count) {
            result.addAll(entities);
            entities = requestMessages(channel, getId(entities.get(0)));//TODO Potentially it can be  reason of infinity loop. Try to filter income messages
        }
        return new ArrayList<>(result);
    }

    private List<ParsedEntity<WebMessage>> requestMessages(String channel) {
        return request("https://t.me/s/" + channel)
                .map(parser::parseMessages)
                .orElseGet(List::of);
    }

    private List<ParsedEntity<WebMessage>> requestMessages(String channel, long beforeId) {
        return request(String.format("https://t.me/s/%s?before=%d", channel, beforeId))
                .map(parser::parseMessages)
                .orElseGet(List::of);
    }

    private Optional<String> request(String url) {
        return limiter.withLimit(() -> client.sendGet(url));
    }

}