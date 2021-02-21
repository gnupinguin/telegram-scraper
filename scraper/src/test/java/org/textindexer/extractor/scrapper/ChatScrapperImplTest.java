package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;
import org.drinkless.tdlib.TdApi;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.textindexer.extractor.messages.MessagesService;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChatScrapperImplTest {

    private static final int CHAT_ID = 1;
    private static final int MESSAGE_ID = 2;

    @Mock
    private MessagesService messagesService;

    @Mock
    private TdApiMessageConverter converter;

    @InjectMocks
    private ChatScrapperImpl scrapper;


    @BeforeClass
    public static void init() {
        System.loadLibrary("tdjni");
    }

    @Test
    public void testScrap() {
        TdApi.Message message = new TdApi.Message();
        message.id = MESSAGE_ID;
        when(messagesService.fetchOld(CHAT_ID, MESSAGE_ID, 1))
                .thenReturn(List.of(message));
        when(converter.convert(any()))
                .thenReturn(new MessageInfo());
        scrapper.scrap(CHAT_ID, MESSAGE_ID, 1);
        verify(converter, times(1)).convert(argThat(m -> m.id == MESSAGE_ID));
    }

    @Test
    public void testScrapDefault() {
        TdApi.Message message = new TdApi.Message();
        message.id = MESSAGE_ID;
        when(messagesService.fetchOld(CHAT_ID, 0, 1))
                .thenReturn(List.of(message));
        when(converter.convert(any()))
                .thenReturn(new MessageInfo());
        scrapper.scrap(CHAT_ID, 1);
        verify(converter, times(1)).convert(argThat(m -> m.id == MESSAGE_ID));
    }

}