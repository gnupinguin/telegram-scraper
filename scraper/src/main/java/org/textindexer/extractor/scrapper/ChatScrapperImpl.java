package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.textindexer.extractor.messages.MessagesService;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class ChatScrapperImpl implements ChatScrapper {

    private static final int DEFAULT_START_CHAT_ID = 0;

    private final MessagesService messagesService;
    private final TdApiMessageConverter converter;

    @Nonnull
    @Override
    public List<MessageInfo> scrap(long chatId, int count) {
        return scrap(chatId, DEFAULT_START_CHAT_ID, count);
    }

    @Nonnull
    @Override
    public List<MessageInfo> scrap(long chatId, long messageId, int count) {
        return messagesService.fetchOld(chatId, messageId, count).stream()
                .map(converter::convert)
                .collect(Collectors.toList());
    }

}
