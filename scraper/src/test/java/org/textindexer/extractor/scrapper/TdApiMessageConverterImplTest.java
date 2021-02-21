package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import org.drinkless.tdlib.TdApi;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TdApiMessageConverterImplTest {

    private static final int CHAT_ID = 1;
    private static final int MESSAGE_ID = 2;
    private static final String URL = "https://t.me/ChannelInLink/12345";
    private static final String URL_IN_TEXT = "https://u.rl2";
    private static final String MENTION = "@mention";
    private static final String TEXT = "text";
    private static final String HASHTAG = "#hashtag";
    private static final String TEXT_CONTENT = String.join(" ", List.of(TEXT, URL, MENTION, HASHTAG));

    private final static int UNIX_DATE = (int)(new Date().getTime() / 1000);

    @InjectMocks
    private TdApiMessageConverterImpl converter;

    @BeforeClass
    public static void init() {
        System.loadLibrary("tdjni");
    }

    @Test
    public void testConvertTextMessage() {
        MessageInfo message = converter.convert(messageWithContent(new TdApi.MessageText(formattedText(), null)));

        assertEquals(MESSAGE_ID, message.getId());
        assertEquals(CHAT_ID, message.getChatId());
        assertEquals(new Date(UNIX_DATE * 1000L), message.getPublishDate());
        assertNotNull(message.getLoadDate());
        assertNull(message.getReplyToMessageId());
        assertNull(message.getForwardedFromMessageId());
        assertEquals(MessageType.Text, message.getType());
        assertEquals(12, message.getViewCount());
        checkTextParts(message);
    }

    private TdApi.FormattedText formattedText() {
        final int urlOffset = TEXT.length() + 1;
        final int mentionOffset = urlOffset + URL.length() + 1;
        final int hashTagOffset = mentionOffset + MENTION.length() + 1;

        return new TdApi.FormattedText(TEXT_CONTENT, new TdApi.TextEntity[]{
                new TdApi.TextEntity(0, TEXT.length(), new TdApi.TextEntityTypeTextUrl(URL_IN_TEXT)),
                new TdApi.TextEntity(urlOffset, URL.length(), new TdApi.TextEntityTypeUrl()),
                new TdApi.TextEntity(mentionOffset, MENTION.length(), new TdApi.TextEntityTypeMention()),
                new TdApi.TextEntity(hashTagOffset, HASHTAG.length(), new TdApi.TextEntityTypeHashtag()),
        });
    }

    private void checkTextParts(MessageInfo message) {
        assertEquals(TEXT_CONTENT, message.getTextContent());

        assertEquals(2, message.getMentions().size());
        assertTrue(message.getMentions().contains(MENTION.substring(1)));
        assertTrue(message.getMentions().contains("ChannelInLink"));

        assertEquals(1, message.getHashTags().size());
        assertTrue(message.getHashTags().contains(HASHTAG.substring(1)));

        assertEquals(2, message.getLinks().size());
        assertTrue(message.getLinks().contains(URL));
        assertTrue(message.getLinks().contains(URL_IN_TEXT));
    }

    @Test
    public void testScrapAudioMessage() {
        MessageInfo message = converter.convert(messageWithContent(new TdApi.MessageAudio(null, formattedText())));
        assertEquals(MessageType.Audio, message.getType());
        checkTextParts(message);
    }

    @Test
    public void testScrapVideoMessage() {
        MessageInfo message = converter.convert(messageWithContent(new TdApi.MessageVideo(null, formattedText(), false)));
        assertEquals(MessageType.Video, message.getType());
        checkTextParts(message);
    }

    @Test
    public void testScrapPhotoMessage() {
        MessageInfo message = converter.convert(messageWithContent(new TdApi.MessagePhoto(null, formattedText(), false)));

        assertEquals(MessageType.Photo, message.getType());
        checkTextParts(message);
    }

    @Test
    public void testScrapDocumentMessage() {
        MessageInfo message = converter.convert(messageWithContent(new TdApi.MessageDocument(null, formattedText())));

        assertEquals(MessageType.Document, message.getType());
        checkTextParts(message);
    }

    @Test
    public void testScrapUndefinedMessageType() {
        MessageInfo message = converter.convert(messageWithContent(new TdApi.MessageExpiredPhoto()));

        assertEquals(MessageType.Other, message.getType());
    }

    @Test
    public void testForwardedMessageWithoutOrigin() {
        TdApi.Message message = baseMessage();
        int forwardedChatId = CHAT_ID + 1;
        int forwardedMessageId = MESSAGE_ID+1;
        message.forwardInfo = new TdApi.MessageForwardInfo(null, UNIX_DATE, "", forwardedChatId, forwardedMessageId);
        MessageInfo info = converter.convert(message);

        assertEquals(forwardedChatId, (long)info.getForwardedFromChatId());
        assertEquals(forwardedMessageId, (long)info.getForwardedFromMessageId());
    }

    @Test
    public void testForwardedMessageWithOrigin() {
        TdApi.Message message = baseMessage();
        int forwardedChatId = CHAT_ID + 1;
        int forwardedMessageId = MESSAGE_ID+1;
        var origin = new TdApi.MessageForwardOriginChannel(forwardedChatId, forwardedMessageId, "author");
        message.forwardInfo = new TdApi.MessageForwardInfo(origin, UNIX_DATE, "", 0, 0);
        MessageInfo info = converter.convert(message);

        assertEquals(forwardedChatId, (long)info.getForwardedFromChatId());
        assertEquals(forwardedMessageId, (long)info.getForwardedFromMessageId());
    }

    @Test
    public void testForwardedMessageWithOriginAndParameters() {
        TdApi.Message message = baseMessage();
        int forwardedChatId = CHAT_ID + 1;
        int forwardedMessageId = MESSAGE_ID+1;
        var origin = new TdApi.MessageForwardOriginChannel(forwardedChatId, forwardedMessageId, "author");
        message.forwardInfo = new TdApi.MessageForwardInfo(origin, UNIX_DATE, "", CHAT_ID, MESSAGE_ID);

        MessageInfo info = converter.convert(message);

        assertEquals(CHAT_ID, (long)info.getForwardedFromChatId());
        assertEquals(MESSAGE_ID, (long)info.getForwardedFromMessageId());
    }



    @Nonnull
    private TdApi.Message baseMessage() {
        TdApi.Message message = new TdApi.Message();
        message.chatId = CHAT_ID;
        message.id = MESSAGE_ID;
        message.date = UNIX_DATE;
        message.replyToMessageId = 0;
        message.interactionInfo = new TdApi.MessageInteractionInfo(12, 13, null);
        return message;
    }

    private TdApi.Message messageWithContent(TdApi.MessageContent content) {
        final TdApi.Message message = baseMessage();
        message.content = content;
        return message;
    }

}