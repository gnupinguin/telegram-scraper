package org.textindexer.extractor.searcher;

import java.util.List;

public interface LanguageDetector {

    boolean detectLanguage(List<String> text);

}
