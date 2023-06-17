package io.github.gnupinguin.tlgscraper.scraper.telegram.parser;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebChannel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Slf4j
public class TelegramHtmlParserImpl implements TelegramHtmlParser {

    private static final Pattern TELEGRAM_CHANNEL_LINK = Pattern.compile("^https://t.me/(\\w{5,32})(?:/(\\d*))?$");

    @Nullable
    @Override
    public ParsedEntity<WebChannel> parseChannel(@Nonnull String html) {
        Document document = getDocument(html);
        Elements titleTag = document.getElementsByTag("title");
        Elements channelTitleTag = document.getElementsByClass("tgme_page_title");
        Elements usersTag = document.getElementsByClass("tgme_page_extra");
        Elements descriptionTag = document.getElementsByClass("tgme_page_description");


        if(isLooksLikeChannel(document)) {
            if (titleTag.size() == 1) {
                String name = titleTag.text().replaceFirst("^.*@", "").trim();
                if (channelTitleTag.size() == 1) {
                    String title = channelTitleTag.text().trim();
                    if (usersTag.size() == 1) {
                        String usersStr = usersTag.text().replaceAll("\\D", "");
                        if (!usersStr.isBlank()) {
                            int users = Integer.parseInt(usersStr);
                            if (descriptionTag.size() == 1) {
                                String description = replaceBrTags(descriptionTag).trim();
                                return parsedEntity(new WebChannel(name, title, StringUtils.substring(description, 0, 300), users), new Date(), descriptionTag);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isLooksLikeChannel(Document document) {
        Elements channelq = document.getElementsByClass("tgme_page_action tgme_page_context_action");
        return channelq.size() == 1 && channelq.text().contains("Preview channel");
    }

    @Nonnull
    @Override
    public List<ParsedEntity<WebMessage>> parseMessages(@Nonnull String html) {
        Document document = getDocument(html);
        return extractChannel(document)
                .map(channel -> parseMessages(document, channel))
                .orElseGet(List::of);
    }

    @Nonnull
    private List<ParsedEntity<WebMessage>> parseMessages(Document document, String channel) {
        Date date = new Date();
        return document.getElementsByClass("tgme_widget_message").stream()
                .map(messageWidget -> parseMessage(date, channel, messageWidget))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    private ParsedEntity<WebMessage> parseMessage(Date date, String channel, Element messageWidget) {
        try {
            Elements textWidget = messageWidget.getElementsByClass("tgme_widget_message_text");
            Integer views = views(messageWidget);
            if (views != null) {
                WebMessage.WebMessageBuilder builder = WebMessage.builder()
                        .id(messageId(messageWidget))
                        .channel(channel)
                        .type(messageType(messageWidget))
                        .textContent(replaceBrTags(textWidget).trim())
                        .publishDate(publishDate(messageWidget))
                        .viewCount(views)
                        .replyToMessageId(replyToMessageId(messageWidget));
                builder = fillForwarding(messageWidget, builder);
                return parsedEntity(builder.build(), date, textWidget);
            }
            return null;
        } catch (RuntimeException e) {
            log.info(messageWidget.html());
            throw e;
        }
    }

    private <T> ParsedEntity<T> parsedEntity(T entity, Date date, Elements content) {
        Set<String> hashTags = new HashSet<>();
        Set<String> links = new HashSet<>();
        Set<String> mentions = new HashSet<>();
        content.select("a").stream()
                .filter(a -> a.attributes().hasKey("href"))
                .forEach(a -> extractMetaInfo(hashTags, links, mentions, a));

        return new ParsedEntity<>(entity, date, mentions, links, hashTags);
    }

    private void extractMetaInfo(Set<String> hashTags, Set<String> links, Set<String> mentions, Element a) {
        Attributes attributes = a.attributes();
        String text = a.text();
        String href = attributes.get("href");
        if (text.startsWith("#")) {
            hashTags.addAll(extractHashTags(text));
        } else if (href.startsWith("http")) {
            if (!text.startsWith("@") && href.length() <= 2048) {
                links.add(href);
                extractLinkMention(href)
                        .ifPresent(mentions::add);
            } else {
                mentions.addAll(extractMentions(text));
            }
        }
    }

    private Set<String> extractMentions(String mentions) {
        return extractMentionOrHashTag(mentions, 32)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private Set<String> extractHashTags(String tags) {
        return extractMentionOrHashTag(tags, 64)
                .collect(Collectors.toSet());
    }

    private Stream<String> extractMentionOrHashTag(String e, int maxLength) {
        return Stream.of(e.split("\\s+"))
                .filter(s -> s.startsWith("#") || s.startsWith("@"))
                .map(s -> s.substring(1))
                .filter(s -> s.length() <= maxLength)
                .filter(not(String::isEmpty));
    }

    private Optional<String> extractChannel(@Nonnull Document document) {
        Elements usernameTag = document.getElementsByClass("tgme_channel_info_header_username");
        if (usernameTag.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(usernameTag.text().substring(1));
    }

    private long messageId(@Nonnull Element messageWidget) {
        String messageIdStr = messageWidget.attributes().get("data-post").replaceFirst("^.*?/", "");
        return Long.parseLong(messageIdStr);
    }

    @Nonnull
    private MessageType messageType(@Nonnull Element messageWidget) {
        Elements videos = messageWidget.getElementsByClass("tgme_widget_message_video");
        Elements photos = messageWidget.getElementsByClass("tgme_widget_message_photo");
        Elements text = messageWidget.getElementsByClass("tgme_widget_message_text");
        Elements document = messageWidget.getElementsByClass("tgme_widget_message_document");
        Elements audio = messageWidget.getElementsByClass("tgme_widget_message_audio");

        if (videos.isEmpty() && photos.isEmpty() && !text.isEmpty()) {
            return MessageType.Text;
        } else if (!videos.isEmpty() && !photos.isEmpty()) {
            return MessageType.Multimedia;
        } else if (!videos.isEmpty()) {
            return MessageType.Video;
        } else if (!photos.isEmpty()) {
            return MessageType.Photo;
        } else if (!document.isEmpty()) {
            return MessageType.Document;
        } else if (!audio.isEmpty()) {
            return MessageType.Audio;
        }
        return MessageType.Other;
    }

    @Nullable
    private Date publishDate(@Nonnull Element messageWidget) {
        Elements messageInfoTag = messageWidget.getElementsByClass("tgme_widget_message_info");
        Elements timeTag = messageInfoTag.select("time");
        if (timeTag.size() == 1) {
            String datetimeAttribute = timeTag.get(0).attributes().get("datetime");
            Instant instant = OffsetDateTime.parse(datetimeAttribute).toInstant();
            return Date.from(instant);
        }
        return null;
    }

    @Nullable
    private Integer views(@Nonnull Element messageWidget) {
        Elements viewsTag = messageWidget.getElementsByClass("tgme_widget_message_views");
        if (!viewsTag.isEmpty()) {
            String text = viewsTag.text();
            double k = 1.0;
            if (text.endsWith("K")) {
                k = 1000;
            } else if (text.endsWith("M")) {
                k = 1_000_000;
            }
            return (int) (Double.parseDouble(text.replaceAll("[MK]", "")) * k);
        }
        return null;
    }

    @Nonnull
    private WebMessage.WebMessageBuilder fillForwarding(Element messageWidget, WebMessage.WebMessageBuilder builder) {
        Elements forwardedWidget = messageWidget.getElementsByClass("tgme_widget_message_forwarded_from_name");
        if (forwardedWidget.size() == 1) {
            Attributes attributes = forwardedWidget.get(0).attributes();
            String href = attributes.get("href");
            Matcher matcher = TELEGRAM_CHANNEL_LINK.matcher(href);
            if (matcher.find()) {
                if (matcher.group(2) != null) {
                    return builder
                            .forwardedFromChannel(matcher.group(1))
                            .forwardedFromMessageId(Long.valueOf(matcher.group(2)));
                }
            }
        }
        return builder;
    }

    @Nullable
    private Long replyToMessageId(@Nonnull Element messageWidget) {
        Elements replyATag = messageWidget.getElementsByClass("tgme_widget_message_reply");
        if (replyATag.size() == 1) {
            Attributes attributes = replyATag.get(0).attributes();
            String href = attributes.get("href");
            Matcher matcher = TELEGRAM_CHANNEL_LINK.matcher(href);
            if (matcher.find()) {
                return Long.valueOf(matcher.group(2));
            }
        }
        return null;
    }

    @Nonnull
    private Document getDocument(@Nonnull String html) {
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        return document;
    }

    @Nonnull
    private String replaceBrTags(@Nonnull Elements elements) {
        elements.select("br").append("\\n");
        return elements.text().replaceAll("\\\\n", "\n");
    }

    private Optional<String> extractLinkMention(@Nonnull String link) {
        Matcher matcher = TELEGRAM_CHANNEL_LINK.matcher(link);
        if (matcher.find()) {
            return Optional.of(matcher.group(1))
                    .map(String::toLowerCase);
        }
        return Optional.empty();
    }

}
