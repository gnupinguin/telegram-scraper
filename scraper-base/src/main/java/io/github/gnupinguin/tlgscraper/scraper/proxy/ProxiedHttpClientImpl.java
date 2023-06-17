package io.github.gnupinguin.tlgscraper.scraper.proxy;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ProxiedHttpClientImpl implements ProxiedHttpClient {

    private final ProxyProvider proxyProvider;
    private final HttpClient client;
    private final ConfusingHeadersProvider confusingHeadersProvider;
    private final AtomicReference<ConfusingHeaders> confusingHeaders;
    private final ProxyClientUpdateValidator proxyValidator;

    private static final int MAX_REQUESTS_COUNT = 18;
    private static final int MAX_ATTEMPTS = 10;

    private final AtomicInteger requestsCount = new AtomicInteger(0);

    public ProxiedHttpClientImpl(ProxyProvider proxyProvider, HttpClient client, ConfusingHeadersProvider confusingHeadersProvider, ProxyClientUpdateValidator proxyValidator) {
        this.proxyProvider = proxyProvider;
        this.client = client;
        this.confusingHeadersProvider = confusingHeadersProvider;
        confusingHeaders = new AtomicReference<>(confusingHeadersProvider.provide());
        this.proxyValidator = proxyValidator;
    }

    @Override
    public synchronized Optional<String> sendGet(String url) {
        if (updateProxy()) {
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
                log.info("Error for request for {}, proxy: {}", url, proxyProvider.next().address(), e);
            }
        }
        return Optional.empty();
    }

    private HttpRequest getRequest(@Nonnull String url) {
        return HttpRequest.newBuilder()
                .header("User-Agent", confusingHeaders.get().uniqueUserAgent())
                .header("Accept", confusingHeaders.get().accept())
                .uri(URI.create(url))
                .GET().build();
    }

    private boolean updateProxy() {
        if (requestsCount.get() > MAX_REQUESTS_COUNT) {
            proxyProvider.forceUpdate();
            for(int i = 0; i < MAX_ATTEMPTS; i++) {
                if (proxyValidator.validate(this::isProxyValid)) {
                    requestsCount.set(0);
                    confusingHeaders.set(confusingHeadersProvider.provide());
                    return true;
                }
            }
            log.info("Valid proxy not found");
            return false;
        } else {
            return true;
        }
    }

    private Optional<String> isProxyValid(String url) {
        try {
            HttpResponse<String> response = client.send(getRequest(url), HttpResponse.BodyHandlers.ofString());
            return Optional.ofNullable(response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
