package io.github.gnupinguin.tlgscraper.scraper.telegram;

import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ProxySource;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ProxySourceSelector;
import io.github.gnupinguin.tlgscraper.scraper.web.ParsedEntity;
import io.github.gnupinguin.tlgscraper.scraper.web.TelegramHtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
@Component
public class MultiProxyTelegramWebClient implements TelegramWebClient {

    private static final String CONFIRMED_CHANNEL = "nexta_live";
    private static final int MAX_REQUESTS_COUNT = 18;
    private static final int MAX_ATTEMPTS = 10;

    private final AtomicInteger requestsCount = new AtomicInteger(0);

    private final HttpClient client;
    private final ProxySource proxySource;
    private final Limiter limiter;
    private final TelegramHtmlParser parser;

    public MultiProxyTelegramWebClient(@Nonnull ProxySource proxySource,
                                       @Nonnull Limiter limiter,
                                       @Nonnull TelegramHtmlParser parser) {
        this.proxySource = proxySource;
        this.limiter = limiter;
        this.client = HttpClient.newBuilder()
                .proxy(new ProxySourceSelector(proxySource))
                .build();
        this.parser = parser;
    }

    @Override
    @Nullable
    public ParsedEntity<Channel> searchChannel(@Nonnull String channel) {
        return onProxy(() -> requestChannel(channel))
                .map(parser::parseChannel)
                .orElse(null);
    }

    @Override
    public List<ParsedEntity<WebMessage>> getLastMessages(@Nonnull String channel, int count) {
        return onProxy(() -> fetchMessages(channel, count))
                .orElseGet(List::of);
    }

    @Nonnull
    private Optional<List<ParsedEntity<WebMessage>>> fetchMessages(@Nonnull String channel, int count) {
        List<ParsedEntity<WebMessage>> result = new ArrayList<>(count);
        List<ParsedEntity<WebMessage>> entities = requestMessages(channel);
        while (!entities.isEmpty() && result.size() < count) {
            Collections.reverse(entities);
            result.addAll(entities);
            ParsedEntity<WebMessage> last = entities.get(entities.size() - 1);
            entities = requestMessages(channel, last.getEntity().getId());
        }
        return Optional.of(result);
    }

    private boolean updateProxy() {
        if (requestsCount.get() > MAX_REQUESTS_COUNT) {
            proxySource.forceUpdate();
            for(int i = 0; i < MAX_ATTEMPTS; i++) {
                if (isChannel(CONFIRMED_CHANNEL)) {
                    requestsCount.set(0);
                    return true;
                }
            }
            log.info("Valid proxy not found");
            return false;
        } else {
            return true;
        }
    }

    private boolean isChannel(@Nonnull String channel) {
        return requestChannel(channel)
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
        return request("https://t.me/s/" + channel)
                .map(parser::parseMessages)
                .orElseGet(List::of);
    }

    private List<ParsedEntity<WebMessage>> requestMessages(String channel, long beforeId) {
        return request(String.format("https://t.me/s/%s?before=%d", channel, beforeId))
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
                } else {
                    log.info("Unsuccessful response for {}: {},\n{}", url, response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.info("Error for request for {}, proxy: {}", url, proxySource.next().address(), e);
            }
            return Optional.empty();
        });
    }

    private HttpRequest getRequest(@Nonnull String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET().build();
    }

}
