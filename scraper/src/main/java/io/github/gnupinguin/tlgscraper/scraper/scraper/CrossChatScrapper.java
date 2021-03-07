package io.github.gnupinguin.tlgscraper.scraper.scraper;

import javax.annotation.Nonnull;
import java.util.List;

public interface CrossChatScrapper {

    void deepScrap(@Nonnull List<String> chatNames);

    void plainScrap(@Nonnull List<String> chatNames);

}
