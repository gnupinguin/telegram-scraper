package io.github.gnupinguin.tlgscraper.scraper.scraper;

import io.github.gnupinguin.tlgscraper.model.db.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import io.github.gnupinguin.tlgscraper.scraper.telegram.TelegramWebClient;
import io.github.gnupinguin.tlgscraper.scraper.telegram.parser.ParsedEntity;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

import java.util.List;



@RequiredArgsConstructor
public class ChannelScrapperImpl implements ChannelScrapper {

    private final TelegramWebClient webClient;
    private final ParsedEntityConverter converter;

    @Nullable
    @Override
    public Channel scrap(@Nonnull String channel, int count) {
        ParsedEntity<WebChannel> parsedChannel = webClient.searchChannel(channel);
        if (parsedChannel != null) {
            List<ParsedEntity<WebMessage>> parsedMessages = webClient.getLastMessages(channel, count);
            return converter.convert(parsedChannel, parsedMessages);
        }
        return null;
    }

    @Override
    public void scrap(@Nonnull Channel channel, long beforeMessageId, int count) {
        List<ParsedEntity<WebMessage>> parsedMessages = webClient.getMessagesBefore(channel.getName(), beforeMessageId, count);
        converter.update(channel, parsedMessages);
    }

}
