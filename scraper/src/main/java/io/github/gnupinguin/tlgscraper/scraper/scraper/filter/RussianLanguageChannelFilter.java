package io.github.gnupinguin.tlgscraper.scraper.scraper.filter;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel;
import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Component
public class RussianLanguageChannelFilter implements ChannelFilter {

    private static final Set<Integer> RUS_CHARS = russianAlphabet();
    private static final int MAX_OTHER_CYRILLIC_COUNT = 5;

    @Override
    public boolean doFilter(@Nonnull Channel chat) {
        log.info("Start language detecting");
        var rusChars = new LongAccumulator(Long::sum, 0);
        long otherCyrillic = chat.getMessages().stream()
                .map(Message::getTextContent)
                .filter(Objects::nonNull)
                .flatMapToInt(String::chars)
                .filter(Character::isAlphabetic)
                .filter(c -> {
                    if (RUS_CHARS.contains(c)) {
                        rusChars.accumulate(1);
                        return false;
                    }
                    return isCyrillic(c);
                })
                .limit(MAX_OTHER_CYRILLIC_COUNT)
                .count();

        return !isOtherCyrillic(otherCyrillic) &&
                (rusChars.get() > 0);
    }

    private boolean isOtherCyrillic(long otherCyrillic) {
        return otherCyrillic >= MAX_OTHER_CYRILLIC_COUNT;
    }

    private static Set<Integer> russianAlphabet() {
        var stream = Stream.concat(
                IntStream.rangeClosed('А', 'Я').boxed(),
                IntStream.rangeClosed('а', 'я').boxed());
        return Stream.concat(Stream.of('Ё', 'ё')
                .map(Integer::valueOf), stream)
                .collect(Collectors.toSet());
    }

    private boolean isCyrillic(int c) {
        return Character.UnicodeBlock.of(c).equals(Character.UnicodeBlock.CYRILLIC);
    }

}
