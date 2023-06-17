package io.github.gnupinguin.tlgscraper.scraper.telegram;

import io.github.gnupinguin.tlgscraper.model.db.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.proxy.ProxiedHttpClient;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.TelegramHtmlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProxiedTelegramWebClientTest {

    @Mock
    private TelegramHtmlParser parser;

    @Mock
    private ProxiedHttpClient client;

    @Mock
    private TelegramRequestLimiter limiter;

    @InjectMocks
    private ProxiedTelegramWebClient telegramWebClient;

    private final String response = "http response";
    private final String channelName = "channel";

    @BeforeEach
    void setUp() {
        when(limiter.withLimit(any()))
                .thenAnswer(invocation -> ((Supplier<?>)invocation.getArgument(0)).get());
    }

    @Test
    void testChannelFound() {
        var parsedChannel = new ParsedEntity<>(getWebChannel(), null, null, null, null);
        when(client.sendGet("https://t.me/" + channelName))
                .thenReturn(Optional.of(response));
        when(parser.parseChannel(response))
                .thenReturn(parsedChannel);
        var result = telegramWebClient.searchChannel(channelName);
        assertEquals(parsedChannel, result);
    }

    private static WebChannel getWebChannel() {
        return new WebChannel("katz", "katz", "Channel of katz", 11);
    }

    @Test
    void testChannelNotFound() {
        when(client.sendGet("https://t.me/" + channelName))
                .thenReturn(Optional.of("error"));
        var result = telegramWebClient.searchChannel(channelName);
        assertNull(result);
    }

    @Test
    void testMessagesFoundAndReversed() {
        when(client.sendGet("https://t.me/s/" + channelName))
                .thenReturn(Optional.of(response));
        List<ParsedEntity<WebMessage>> messages = List.of(parsedMessage(1), parsedMessage(2));
        when(parser.parseMessages(response))
                .thenReturn(messages);

        List<ParsedEntity<WebMessage>> lastMessages = telegramWebClient.getLastMessages(channelName, 1);
        System.out.println(lastMessages);
        assertEquals(messages.get(0), lastMessages.get(1));
        assertEquals(messages.get(1), lastMessages.get(0));
    }

    @Test
    void testMessagesFoundManyRequests() {
        when(client.sendGet("https://t.me/s/" + channelName))
                .thenReturn(Optional.of(response));
        String beforeMessagesResponse = response + 1;
        when(client.sendGet("https://t.me/s/" + channelName + "?before=1"))
                .thenReturn(Optional.of(beforeMessagesResponse));

        List<ParsedEntity<WebMessage>> messages = List.of(parsedMessage(1), parsedMessage(2));
        when(parser.parseMessages(response))
                .thenReturn(List.of(messages.get(0)));
        when(parser.parseMessages(beforeMessagesResponse))
                .thenReturn(List.of(messages.get(1)));

        List<ParsedEntity<WebMessage>> lastMessages = telegramWebClient.getLastMessages(channelName, 2);
        assertEquals(messages.get(0), lastMessages.get(1));
        assertEquals(messages.get(1), lastMessages.get(0));
    }

    @Test
    void testRemoveDuplicates() {
        when(client.sendGet("https://t.me/s/" + channelName))
                .thenReturn(Optional.of(response));
        String beforeMessagesResponse = response + 1;
        when(client.sendGet("https://t.me/s/" + channelName + "?before=1"))
                .thenReturn(Optional.of(beforeMessagesResponse));

        List<ParsedEntity<WebMessage>> messages = List.of(parsedMessage(1), parsedMessage(1));
        when(parser.parseMessages(response))
                .thenReturn(List.of(messages.get(0)));
        when(parser.parseMessages(beforeMessagesResponse))
                .thenReturn(List.of(messages.get(1)), List.of());//TODO Potentially it can be  reason of infinity loop. Try to filter income messages

        List<ParsedEntity<WebMessage>> lastMessages = telegramWebClient.getLastMessages(channelName, 2);
        assertEquals(List.of(messages.get(0)), lastMessages);
    }

    private ParsedEntity<WebMessage> parsedMessage(long id) {
       WebMessage webMessage = WebMessage.builder()
               .id(id)
               .build();
       return new ParsedEntity<>(webMessage, new Date(), null, null, null);
    }

}