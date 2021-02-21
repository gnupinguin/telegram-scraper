package org.textindexer.extractor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.textindexer.extractor.searcher.RussianBelorussianLanguageDetector;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RussianBelorussianLanguageDetectorTest {

    @InjectMocks
    private RussianBelorussianLanguageDetector detector;

    @Test
    public void testDetectRussian() {
        assertTrue(detector.detectLanguage(List.of("привет, мой друг!!!!!!!!!!!!!!")));
    }

    @Test
    public void testDetectBelorussian() {
        assertTrue(detector.detectLanguage(List.of("здароў, палітыкі ііі")));
    }

    @Test
    public void testDetectEnglish() {
        assertFalse(detector.detectLanguage(List.of("Hello, world!")));
    }

    @Test
    public void testDetectUz() {
        assertFalse(detector.detectLanguage(List.of("соғлиқни сақлаш ққ ")));
    }

    @Test
    public void testDetect1() {
        assertTrue(detector.detectLanguage(List.of("Ё")));
    }

}