package org.textindexer.extractor.searcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Component
public class RussianBelorussianLanguageDetector implements LanguageDetector {

    private static final Integer RUS = 0;
    private static final Integer BY = 1;
    private static final Integer OTHER_CYRILLIC = 2;
    private static final Integer OTHER = 3;

    private static final Set<Integer> BY_CHARS = belorussianAdditional().collect(Collectors.toSet());
    private static final Set<Integer> RUS_CHARS = russian().collect(Collectors.toSet());


    @Override
    public boolean detectLanguage(List<String> messages) {
        log.info("Start language detecting");
        Set<Character> brokenCyrillic = new HashSet<>();
        Map<Integer, Integer> chars = defaultMap();

        messages.stream()
                .filter(Objects::nonNull)
                .flatMapToInt(String::chars)
                .filter(Character::isAlphabetic)
                .boxed()
                .forEach(c -> {
                    if (RUS_CHARS.contains(c)) {
                       chars.compute(RUS, (k, v) -> v + 1);
                    } else if (BY_CHARS.contains(c)) {
                        chars.compute(BY, (k, v) -> v + 1);
                    } else if (isCyrillic(c)) {
                        chars.compute(OTHER_CYRILLIC, (k, v) -> v + 1);
                        brokenCyrillic.add((char) c.intValue());
                    } else {
                        chars.compute(OTHER, (k, v) -> v + 1);
                    }
                });

        if (chars.get(OTHER_CYRILLIC) >=  5) {
            log.info("Many other cyrillic characters: {}", brokenCyrillic );
            return false;
        }

        return chars.get(RUS) > 0;
    }


    private Map<Integer, Integer> defaultMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(BY, 0);
        map.put(OTHER_CYRILLIC, 0);
        map.put(OTHER, 0);
        map.put(RUS, 0);
        return map;
    }

    @Nonnull
    private static Stream<Integer> belorussianAdditional() {
        return Stream.of('i', 'I', 'ў', 'Ў', 'Ё', 'ё', 'і', Character.toUpperCase('і')).map(Integer::valueOf);
    }

    private static Stream<Integer> russian() {
        var stream = Stream.concat(
                IntStream.rangeClosed('А', 'Я').boxed(),
                IntStream.rangeClosed('а', 'я').boxed());
        return Stream.concat(Stream.of('Ё', 'ё').map(Integer::valueOf), stream);
    }

    private boolean isCyrillic(int c) {
        return Character.UnicodeBlock.of(c).equals(Character.UnicodeBlock.CYRILLIC);
    }

}
