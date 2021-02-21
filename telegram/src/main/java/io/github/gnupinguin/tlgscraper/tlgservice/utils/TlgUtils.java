package io.github.gnupinguin.tlgscraper.tlgservice.utils;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public final class TlgUtils {

    @Nonnull
    public static Supplier<String> fileAuthCodeSource(String fileName){
        return () -> {
            try {
                var path = Path.of(fileName);
                if (!path.toFile().exists()) {
                    path.toFile().createNewFile();
                }
                Files.writeString(path, "", StandardOpenOption.TRUNCATE_EXISTING);
                for (int i = 0; i < 15; i++) {
                    System.out.println("Please, input authorization code and symbol '!' after it to " + path.toFile().getAbsolutePath());
                    final var text = Files.readString(path).replaceAll("\\s", "");
                    if (text.endsWith("!")) {
                        return text.substring(0, text.length() - 1);
                    }
                    TimeUnit.SECONDS.sleep(4);
                }
            } catch (Exception e) {
                System.out.println("Error happened during authorization");
                throw new RuntimeException(e);
            }
            System.out.println("Authorization code not found");
            throw new RuntimeException();
        };
    }


}
