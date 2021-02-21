package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageInfo;
import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import org.drinkless.tdlib.TdApi;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TdApiMessageConverterImpl implements TdApiMessageConverter {

    private static final Pattern TELEGRAM_CHANNEL_LINK = Pattern.compile("^https://t.me/(\\w{5,32})(?:/\\d*)?$");

    @Nonnull
    @Override
    public MessageInfo convert(@Nonnull TdApi.Message message) {
        final MessageInfo result = new MessageInfo();
        result.setChatId(message.chatId);
        result.setId(message.id);
        result.setPublishDate(convertUnixTimestamp(message));
        result.setLoadDate(new Date());
        result.setReplyToMessageId(getReplyToMessageId(message));
        result.setViewCount(getViewCount(message));
        fillForwarded(message, result);
        fillTypeAndContent(message, result);

        return result;
    }

    private int getViewCount(TdApi.Message message) {
        if (message.interactionInfo != null) {
            return message.interactionInfo.viewCount;
        }
        return 0;
    }

    @Nullable
    private Long getReplyToMessageId(TdApi.Message message) {
        return message.replyToMessageId == 0 ? null : message.replyToMessageId;
    }

    @Nonnull
    private Date convertUnixTimestamp(TdApi.Message message) {
        return new Date(message.date * 1000L);
    }

    private void fillForwarded(TdApi.Message message, MessageInfo result) {
        final TdApi.MessageForwardInfo forwardInfo = message.forwardInfo;
        if (forwardInfo != null) {
            if (forwardInfo.fromChatId != 0) {
                result.setForwardedFromChatId(forwardInfo.fromChatId);
                result.setForwardedFromMessageId(forwardInfo.fromMessageId);
            } else if (forwardInfo.origin != null) {
                if (forwardInfo.origin instanceof TdApi.MessageForwardOriginChannel) {
                    var origin = (TdApi.MessageForwardOriginChannel)forwardInfo.origin;
                    result.setForwardedFromChatId(origin.chatId);
                    result.setForwardedFromMessageId(origin.messageId);
                }
            }
        }
    }

    void fillTypeAndContent(TdApi.Message message, MessageInfo messageInfo) {
        final TdApi.MessageContent content = message.content;
        if (content instanceof TdApi.MessageText) {
            final var contentEntity = (TdApi.MessageText) message.content;
            messageInfo.setType(MessageType.Text);
            fillContent(contentEntity.text, messageInfo);
        } else if (content instanceof TdApi.MessageAudio) {
            final var contentEntity = (TdApi.MessageAudio) message.content;
            messageInfo.setType(MessageType.Audio);
            fillContent(contentEntity.caption, messageInfo);
        } else if (content instanceof TdApi.MessageDocument) {
            final var contentEntity = (TdApi.MessageDocument) message.content;
            messageInfo.setType(MessageType.Document);
            fillContent(contentEntity.caption, messageInfo);
        } else if (content instanceof TdApi.MessagePhoto) {
            final var contentEntity = (TdApi.MessagePhoto) message.content;
            messageInfo.setType(MessageType.Photo);
            fillContent(contentEntity.caption, messageInfo);
        } else if (content instanceof TdApi.MessageVideo) {
            final var contentEntity = (TdApi.MessageVideo) message.content;
            messageInfo.setType(MessageType.Video);
            fillContent(contentEntity.caption, messageInfo);
        } else {
            messageInfo.setType(MessageType.Other);
            fillContent(null, messageInfo);
        }
    }

    private void fillContent(@Nullable TdApi.FormattedText formattedText, @Nonnull MessageInfo messageInfo) {
        List<String> links = new ArrayList<>();
        List<String> mentions = new ArrayList<>();
        List<String> hashTags = new ArrayList<>();

        if (formattedText != null) {
            String text = formattedText.text == null ? "" : formattedText.text;
            messageInfo.setTextContent(text);
            for (TdApi.TextEntity entity : formattedText.entities) {
                if (isMention(entity)) {
                    mentions.add(extractFromEntity(text, entity, true));
                } else if (isHashTag(entity)) {
                    hashTags.add(extractFromEntity(text, entity, true));
                } else if (isTextUrl(entity)) {
                    final String url = ((TdApi.TextEntityTypeTextUrl) entity.type).url;
                    if (url != null) {
                        links.add(url);
                        extractMentionFromLink(url).ifPresent(mentions::add);
                    }
                } else if(isUrl(entity)) {
                    String url = extractFromEntity(text, entity, false);
                    links.add(url);
                    extractMentionFromLink(url).ifPresent(mentions::add);
                }
            }
        }

        messageInfo.setLinks(links);
        messageInfo.setMentions(mentions);
        messageInfo.setHashTags(hashTags);
    }

    private Optional<String> extractMentionFromLink(String url) {
        Matcher matcher = TELEGRAM_CHANNEL_LINK.matcher(url);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    private boolean isUrl(TdApi.TextEntity entity) {
        return entity.type instanceof TdApi.TextEntityTypeUrl;
    }

    private boolean isTextUrl(TdApi.TextEntity entity) {
        return entity.type instanceof TdApi.TextEntityTypeTextUrl;
    }

    @Nonnull
    private String extractFromEntity(String text, TdApi.TextEntity entity, boolean removeFirstChar) {
        int bias = removeFirstChar ? 1 : 0;
        return text.substring(entity.offset + bias, entity.offset + entity.length);
    }

    private boolean isHashTag(TdApi.TextEntity entity) {
        return entity.type instanceof TdApi.TextEntityTypeHashtag;
    }

    private boolean isMention(TdApi.TextEntity entity) {
        return entity.type instanceof TdApi.TextEntityTypeMention;
    }

}
