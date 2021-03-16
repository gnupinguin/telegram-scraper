package io.github.gnupinguin.tlgscraper.db.repository;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class DbTools {

    public static final Function<List<Object>, Long> ID_GENERATOR_MAPPER = l -> (Long)l.get(0);

    public static String questionMarks(int count) {
        String marks = IntStream.rangeClosed(1, count)
                .mapToObj(i -> "?")
                .collect(Collectors.joining(", "));
        return "(" + marks  + ")";
    }

}
