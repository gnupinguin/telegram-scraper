package org.textindexer.extractor.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.textindexer.extractor.proxy.ProxySource;
import org.textindexer.extractor.proxy.ProxySourceSelector;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class MultiProxyTelegramWebClient implements TelegramWebClient {

    private final HttpClient client;
    private final ProxySource proxySource;

    private static final String CONFIRMED_CHANNEL = "nexta_live";
    private static final int MAX_REQUESTS_COUNT = 5;
    private static final int MAX_ATTEMPTS_PROXY_SELECTION = 10;

    private final Lock lock = new ReentrantLock();
    private int requestsCount = 0;


    public MultiProxyTelegramWebClient(ProxySource proxySource) {
        this.proxySource = proxySource;
        this.client = HttpClient.newBuilder()
                .proxy(new ProxySourceSelector(proxySource))
                .build();
    }

    @Override
    public boolean channelPresent(String channel) {
        try {
            lock.lock();
            if (updateProxy()) {
                TimeUnit.SECONDS.sleep(1);
                requestsCount++;
                return isChannel(channel);
            }

        } catch (InterruptedException e) {
            //ignore
        } finally {
            lock.unlock();
        }
        return false;
    }

    private boolean updateProxy() {
        if (requestsCount >= MAX_REQUESTS_COUNT) {
            proxySource.forceUpdate();
            log.info("Proxy was updated");
            for (int i = 0; i < MAX_ATTEMPTS_PROXY_SELECTION; i++) {
                if (isChannel(CONFIRMED_CHANNEL)) {
                    requestsCount = 0;
                    return true;
                }
            }
            log.info("Valid proxy not found");
            return false;
        }
        return true;
    }

    private boolean isChannel(@Nonnull String channel) {
        try {
            HttpResponse<String> response = requestChannel(channel);
            if (response.statusCode() == 200) {
                if (response.body().contains("Preview channel")) {
                    return true;
                }
            } else {
                log.info("Unsuccessful response for '{}': {},\n{}", channel, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.info("Error for request to Web for channel '{}', proxy: {}", channel, proxySource.next().address(), e);
        }
        return false;
    }

    private HttpResponse<String> requestChannel(@Nonnull String name) throws java.io.IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://t.me/" + name))
                .timeout(Duration.ofMinutes(1))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
