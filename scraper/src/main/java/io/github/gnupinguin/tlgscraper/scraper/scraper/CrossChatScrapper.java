package io.github.gnupinguin.tlgscraper.scraper.scraper;

import javax.annotation.Nonnull;
import java.util.List;

public interface CrossChatScrapper {

    void scrapFromQueue();

    void plainScrap(@Nonnull List<String> chatNames);

}
