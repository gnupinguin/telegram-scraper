package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import io.github.gnupinguin.tlgscraper.scraper.scraper.model.WebChannel;
import io.github.gnupinguin.tlgscraper.scraper.scraper.model.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.TelegramWebClient;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelScrapperImplTest {

    public static final String CHANNEL = "channel";

    @Mock
    private ParsedEntityConverter converter;

    @Mock
    private TelegramWebClient webClient;

    @InjectMocks
    private ChannelScrapperImpl scrapper;

    @Test
    public void testSuccessful() {
        var channelParsedEntity = new ParsedEntity<WebChannel>(null, new Date(), Set.of(), Set.of(), Set.of());
        when(webClient.searchChannel(CHANNEL))
                .thenReturn(channelParsedEntity);

        List<ParsedEntity<WebMessage>> messages = List.of();
        when(webClient.getLastMessages(CHANNEL, 300))
                .thenReturn(messages);

        var chat = Channel.builder().build();
        when(converter.convert(channelParsedEntity, messages))
                .thenReturn(chat);
        assertEquals(chat, scrapper.scrap(CHANNEL, 300));
    }

    @Test
    public void testChannelNotFound() {
        when(webClient.searchChannel(CHANNEL))
                .thenReturn(null);
        assertNull(scrapper.scrap(CHANNEL, 300));
    }

}