package io.github.gnupinguin.tlgscraper.scraper.scraper.filter;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Component
public class RussianLanguageChatFilter implements ChatFilter {

    private static final Set<Integer> RUS_CHARS = russian();
    private static final int MAX_OTHER_CYRILLIC_COUNT = 5;

    @Override
    public boolean doFilter(@Nonnull Chat chat) {
        log.info("Start language detecting");
        AtomicInteger rusChars = new AtomicInteger(0);
        long otherCyrillic = chat.getMessages().stream()
                .map(Message::getTextContent)
                .filter(Objects::nonNull)
                .flatMapToInt(String::chars)
                .filter(Character::isAlphabetic)
                .filter(c -> {
                    if (RUS_CHARS.contains(c)) {
                        rusChars.incrementAndGet();
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

    private static Set<Integer> russian() {
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
