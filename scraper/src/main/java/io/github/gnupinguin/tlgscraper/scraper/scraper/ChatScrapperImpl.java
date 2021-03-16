package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.Chat;
import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.TelegramWebClient;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ChatScrapperImpl implements ChatScrapper {

    private final TelegramWebClient webClient;
    private final ParsedEntityConverter converter;

    @Nullable
    @Override
    public Chat scrap(@Nonnull String channel, int count) {
        ParsedEntity<Channel> parsedChannel = webClient.searchChannel(channel);
        if (parsedChannel != null) {
            List<ParsedEntity<WebMessage>> parsedMessages = webClient.getLastMessages(channel, count);
            return converter.convert(parsedChannel, parsedMessages);
        }
        return null;
    }

    @Override
    public void scrap(@Nonnull Chat channel, long beforeMessageId, int count) {
        List<ParsedEntity<WebMessage>> parsedMessages = webClient.getMessagesBefore(channel.getName(), beforeMessageId, count);
        converter.update(channel, parsedMessages);
    }

}
