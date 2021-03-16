package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.scraper.utils.ScraperProfiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile(ScraperProfiles.DEEP)
@Component
@RequiredArgsConstructor
public class DeepCrossChatScraperImpl implements CrossChatScraper {

    @Override
    public void scrap() {

    }

}
