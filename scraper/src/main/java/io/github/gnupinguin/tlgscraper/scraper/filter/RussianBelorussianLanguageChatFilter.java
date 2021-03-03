package io.github.gnupinguin.tlgscraper.scraper.filter;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.db.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Component
public class RussianBelorussianLanguageChatFilter implements ChatFilter {

    private static final Set<Integer> BY_CHARS = belorussianAdditional();
    private static final Set<Integer> RUS_CHARS = russian();

    @Override
    public boolean doFilter(@Nonnull Chat chat) {
        log.info("Start language detecting");
        Set<Character> brokenCyrillic = new HashSet<>();

        AtomicInteger rusChars = new AtomicInteger(0);
        AtomicInteger otherCyrillicChars = new AtomicInteger(0);
        AtomicInteger otherChars = new AtomicInteger(0);

        chat.getMessages().stream()
                .map(Message::getTextContent)
                .filter(Objects::nonNull)
                .flatMapToInt(String::chars)
                .filter(Character::isAlphabetic)
                .forEach(c -> {
                    if (RUS_CHARS.contains(c)) {
                       rusChars.incrementAndGet();
                    } else if (BY_CHARS.contains(c)) {
                        //ignore
                    } else if (isCyrillic(c)) {
                        otherCyrillicChars.incrementAndGet();
                        brokenCyrillic.add((char) c);
                    } else {
                        otherChars.incrementAndGet();
                    }
                });

        if (otherCyrillicChars.get() >= 5) {
            log.info("Many other cyrillic characters: {}", brokenCyrillic );
            return false;
        }

        return rusChars.get() > 0;
    }

    @Nonnull
    private static Set<Integer> belorussianAdditional() {
        return Stream.of('i', 'I', 'ў', 'Ў', 'Ё', 'ё', 'і', Character.toUpperCase('і'))
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
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
