package io.github.gnupinguin.tlgscraper.scraper.telegram;

import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ProxySource;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ProxySourceSelector;
import io.github.gnupinguin.tlgscraper.scraper.proxy.UserAgentSource;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.TelegramHtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProxiedTelegramWebClient implements TelegramWebClient {

    private static final String CONFIRMED_CHANNEL = "nexta_live";
    private static final int MAX_REQUESTS_COUNT = 18;
    private static final int MAX_ATTEMPTS = 10;

    private final AtomicInteger requestsCount = new AtomicInteger(0);
    private final AtomicReference<String> userAgent;

    private final HttpClient client;
    private final ProxySource proxySource;
    private final UserAgentSource userAgentSource;
    private final TelegramRequestLimiter limiter;
    private final TelegramHtmlParser parser;

    public ProxiedTelegramWebClient(@Nonnull ProxySource proxySource,
                                    @Nonnull UserAgentSource userAgentSource,
                                    @Nonnull TelegramRequestLimiter limiter,
                                    @Nonnull TelegramHtmlParser parser) {
        this.proxySource = proxySource;
        this.userAgentSource = userAgentSource;
        this.limiter = limiter;
        this.client = HttpClient.newBuilder()
                .proxy(new ProxySourceSelector(proxySource))
                .build();
        this.parser = parser;
        userAgent = new AtomicReference<>(userAgentSource.nextUserAgent());
    }

    @Override
    @Nullable
    public ParsedEntity<Channel> searchChannel(@Nonnull String channel) {
        return onProxy(() -> requestChannel(channel))
                .map(parser::parseChannel)
                .orElse(null);
    }

    @Nonnull
    @Override
    public List<ParsedEntity<WebMessage>> getLastMessages(@Nonnull String channel, int count) {
        Set<ParsedEntity<WebMessage>> result = new HashSet<>(count + 18);
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

    private boolean updateProxy() {
        if (requestsCount.get() > MAX_REQUESTS_COUNT) {
            proxySource.forceUpdate();
            for(int i = 0; i < MAX_ATTEMPTS; i++) {
                if (confirmedChannelFound()) {
                    requestsCount.set(0);
                    userAgent.set(userAgentSource.nextUniqueAgent());
                    return true;
                }
            }
            log.info("Valid proxy not found");
            return false;
        } else {
            return true;
        }
    }

    private boolean confirmedChannelFound() {
        return requestChannel(CONFIRMED_CHANNEL)
                .map(response -> response.contains("Preview channel"))
                .orElse(false);
    }

    private synchronized <T> Optional<T> onProxy(Supplier<Optional<T>> supplier) {
        if (updateProxy()) {
            return supplier.get();
        }
        return Optional.empty();
    }

    private Optional<String> requestChannel(String channel) {
        return request("https://t.me/" + channel);
    }

    private List<ParsedEntity<WebMessage>> requestMessages(String channel) {
        return onProxy(() -> request("https://t.me/s/" + channel))
                .map(parser::parseMessages)
                .orElseGet(List::of);

    }

    private List<ParsedEntity<WebMessage>> requestMessages(String channel, long beforeId) {
        return onProxy(() -> request(String.format("https://t.me/s/%s?before=%d", channel, beforeId)))
                .map(parser::parseMessages)
                .orElseGet(List::of);
    }

    private Optional<String> request(@Nonnull String url) {
        requestsCount.incrementAndGet();
        return limiter.withLimit(() -> {
            try {
                HttpRequest request = getRequest(url);
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return Optional.ofNullable(response.body());
                } if (response.statusCode() >= 500) {
                    log.info("Unsuccessful response for {}: {}. Waiting 3s", url, response.statusCode());
                    TimeUnit.SECONDS.sleep(3);
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        return Optional.ofNullable(response.body());
                    }
                }
                log.info("Unsuccessful response for {}: {},\n{}", url, response.statusCode(), response.body());
            } catch (Exception e) {
                log.info("Error for request for {}, proxy: {}", url, proxySource.next().address(), e);
            }
            return Optional.empty();
        });
    }

    private HttpRequest getRequest(@Nonnull String url) {
        return HttpRequest.newBuilder()
                .header("User-Agent", userAgent.get())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .uri(URI.create(url))
                .GET().build();
    }

}
