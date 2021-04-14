package io.github.gnupinguin.analyzer.document;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class VocabularyTermsModel implements TermsModel {

    private final Map<String, Integer> vocabulary;

    public VocabularyTermsModel(@Nonnull String[] terms) {
        vocabulary = IntStream.range(0, terms.length)
                .collect(HashMap::new, (m, i) -> m.put(terms[i], i), HashMap::putAll);
    }

    @Override
    public int termIdentifier(@Nonnull String word) {
        return vocabulary.getOrDefault(word, -1);
    }

}
