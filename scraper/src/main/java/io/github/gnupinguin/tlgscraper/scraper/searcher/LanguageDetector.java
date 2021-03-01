package io.github.gnupinguin.tlgscraper.scraper.searcher;

import java.util.List;

public interface LanguageDetector {

    boolean detectLanguage(List<String> text);

}
