package io.github.gnupinguin.tlgscraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Message;
import io.github.gnupinguin.tlgscraper.scraper.scraper.filter.RussianLanguageChannelFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RussianLanguageChannelFilterTest {

    @InjectMocks
    private RussianLanguageChannelFilter detector;

    @Test
    public void testDetectRussian() {
        assertTrue(detector.doFilter(getChat("привет, мой друг!!!!!!!!!!!!!!")));
    }

    @Test
    public void testDetectBelorussian() {
        assertFalse(detector.doFilter(getChat("здароў, палітыкі ііі")));
    }

    @Test
    public void testDetectEnglish() {
        assertFalse(detector.doFilter(getChat("Hello, world!")));
    }

    @Test
    public void testDetectUz() {
        assertFalse(detector.doFilter(getChat("соғлиқни сақлаш ққ ")));
    }

    @Test
    public void testDetect1() {
        assertTrue(detector.doFilter(getChat("Ё")));
    }

    private Channel getChat(String message) {
        return Channel.builder()
                .messages(List.of(Message.builder()
                        .textContent(message)
                        .build()))
                .build();
    }

}