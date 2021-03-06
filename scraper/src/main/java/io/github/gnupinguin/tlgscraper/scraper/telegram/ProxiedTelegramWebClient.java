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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProxiedTelegramWebClient implements TelegramWebClient {

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
        int initialCapacity = count + count % 18;
        Set<ParsedEntity<WebMessage>> result = new HashSet<>(initialCapacity);
        List<ParsedEntity<WebMessage>> entities = filterRequest(result, () -> requestMessages(channel));
        while (!entities.isEmpty() && result.size() < count) {
            result.addAll(entities);
            ParsedEntity<WebMessage> last = entities.get(entities.size() - 1);
            long lastId = last.getEntity().getId();
            entities = filterRequest(result, () -> requestMessages(channel, lastId));
        }
        return new ArrayList<>(result);
    }

    @Nonnull
    List<ParsedEntity<WebMessage>> filterRequest(Set<ParsedEntity<WebMessage>> result,
                                                 Supplier<List<ParsedEntity<WebMessage>>> request) {
        List<ParsedEntity<WebMessage>> parsedEntities = request.get().stream()
                .filter(Predicate.not(result::contains))//TODO investigate situation
                .distinct()
                .collect(Collectors.toList());
        Collections.reverse(parsedEntities);
        return parsedEntities;
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

    public Optional<String> request(String url) {
        return limiter.withLimit(() -> client.sendGet(url));
    }

}