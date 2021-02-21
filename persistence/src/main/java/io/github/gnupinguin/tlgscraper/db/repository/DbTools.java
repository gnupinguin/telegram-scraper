package io.github.gnupinguin.tlgscraper.db.repository;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class DbTools {

    public static String questionMarks(int count) {
        String marks = IntStream.rangeClosed(1, count)
                .mapToObj(i -> "?")
                .collect(Collectors.joining(", "));
        return "(" + marks  + ")";
    }

}
