package io.github.gnupinguin.tlgscraper.scraper.scraper;


import io.github.gnupinguin.tlgscraper.model.db.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.TelegramWebClient;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChannelScrapperImplTest {

    private static final String CHANNEL = "channel";

    @Mock
    ParsedEntityConverter converter;

    @Mock
    TelegramWebClient webClient;

    @InjectMocks
    ChannelScrapperImpl scrapper;

    @Test
    void testSuccessful() {
        ParsedEntity<WebChannel> channelParsedEntity = new ParsedEntity<>(null, new Date(), Set.of(), Set.of(), Set.of());
        when(webClient.searchChannel(CHANNEL))
                .thenReturn(channelParsedEntity);

        List<ParsedEntity<WebMessage>> messages = List.of();
        when(webClient.getLastMessages(CHANNEL, 300))
                .thenReturn(messages);

        Channel chat = Channel.builder().build();
        when(converter.convert(channelParsedEntity, messages))
                .thenReturn(chat);
        assertEquals(chat, scrapper.scrap(CHANNEL, 300));
    }

    @Test
    void testChannelNotFound() {
        when(webClient.searchChannel(CHANNEL))
                .thenReturn(null);
        assertNull(scrapper.scrap(CHANNEL, 300));
    }

}