package io.github.gnupinguin.tlgscraper.scraper.telegram.parser;

import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TelegramHtmlParserImplTest {

    private Path correctChannel;
    private Path invalidChannel;
    private Path messagesPage;

    @InjectMocks
    private TelegramHtmlParserImpl parser;

    @BeforeEach
    void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        correctChannel = Path.of(classLoader.getResource("channel.html").getFile());
        invalidChannel = Path.of(classLoader.getResource("invalid_channel.html").getFile());
        messagesPage = Path.of(classLoader.getResource("nexta.html").getFile());
    }

    @Test
    void testCorrectParseChannel() throws Exception {
        String html = Files.readString(correctChannel);
        assertNotNull(parser.parseChannel(html));
    }

    @Test
    void testInvalidParseChannel() throws Exception {
        String html = Files.readString(invalidChannel);
        assertNull(parser.parseChannel(html));
    }
    
    @Test
    void testMessages() throws Exception {
        String html = Files.readString(messagesPage);
        List<ParsedEntity<WebMessage>> parsedEntities = parser.parseMessages(html);
        assertEquals(18, parsedEntities.size());
    }

    @Test
    void testMessagesNotFound() throws Exception {
        String html = Files.readString(correctChannel);
        List<ParsedEntity<WebMessage>> parsedEntities = parser.parseMessages(html);
        assertTrue(parsedEntities.isEmpty());
    }

}