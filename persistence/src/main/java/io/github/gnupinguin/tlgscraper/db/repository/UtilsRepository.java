package io.github.gnupinguin.tlgscraper.db.repository;

import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class UtilsRepository {

    private final QueryExecutor queryExecutor;

    @Nonnull
    public Date getCurrentDbDate() {
        var date = queryExecutor.getCurrentDate();
        return new Date(date.getTime());
    }

}
