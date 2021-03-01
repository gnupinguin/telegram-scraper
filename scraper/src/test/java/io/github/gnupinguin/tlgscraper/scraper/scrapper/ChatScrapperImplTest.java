package io.github.gnupinguin.tlgscraper.scraper.scrapper;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChatScrapperImplTest {

    private static final int CHAT_ID = 1;
    private static final int MESSAGE_ID = 2;


    @InjectMocks
    private ChatScrapperImpl scrapper;


    @BeforeClass
    public static void init() {
        System.loadLibrary("tdjni");
    }


}