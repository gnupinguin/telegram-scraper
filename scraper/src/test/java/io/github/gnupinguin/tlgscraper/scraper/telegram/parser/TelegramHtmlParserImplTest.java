package io.github.gnupinguin.tlgscraper.scraper.telegram.parser;

import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class TelegramHtmlParserImplTest {

    private File correctChannel;
    private File invalidChannel;
    private File messagesPage;

    @InjectMocks
    private TelegramHtmlParserImpl parser;

    @Before
    public void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        correctChannel = new File(classLoader.getResource("channel.html").getFile());
        invalidChannel = new File(classLoader.getResource("invalid_channel.html").getFile());
        messagesPage = new File(classLoader.getResource("nexta.html").getFile());
    }

    @Test
    public void testCorrectParseChannel() throws Exception {
        String html = readFileToString(correctChannel, Charset.defaultCharset());
        assertNotNull(parser.parseChannel(html));
    }

    @Test
    public void testInvalidParseChannel() throws Exception {
        String html = readFileToString(invalidChannel, Charset.defaultCharset());
        assertNull(parser.parseChannel(html));
    }
    
    @Test
    public void testMessages() throws Exception {
        String html = readFileToString(messagesPage, Charset.defaultCharset());
        List<ParsedEntity<WebMessage>> parsedEntities = parser.parseMessages(html);
        assertEquals(18, parsedEntities.size());
    }

    @Test
    public void testMessagesNotFound() throws Exception {
        String html = readFileToString(correctChannel, Charset.defaultCharset());
        List<ParsedEntity<WebMessage>> parsedEntities = parser.parseMessages(html);
        assertTrue(parsedEntities.isEmpty());
    }

}