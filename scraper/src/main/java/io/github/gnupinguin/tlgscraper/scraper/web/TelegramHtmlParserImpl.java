package io.github.gnupinguin.tlgscraper.scraper.web;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import io.github.gnupinguin.tlgscraper.model.scraper.web.Channel;
import io.github.gnupinguin.tlgscraper.model.scraper.web.WebMessage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramHtmlParserImpl implements TelegramHtmlParser {

    private static final Pattern TELEGRAM_CHANNEL_LINK = Pattern.compile("^https://t.me/(\\w{5,32})(?:/(\\d*))?$");

    @Override
    public ParsedEntity<Channel> parseChannel(@Nonnull String html) {
        Document document = getDocument(html);
        Elements titleTag = document.getElementsByTag("title");
        Elements channelTitleTag = document.getElementsByClass("tgme_page_title");
        Elements usersTag = document.getElementsByClass("tgme_page_extra");
        Elements descriptionTag = document.getElementsByClass("tgme_page_description");

        if (titleTag.size() == 1) {
            String name = titleTag.text().replaceFirst("^.*@", "");
            if (channelTitleTag.size() == 1) {
                String title = channelTitleTag.text();
                if (usersTag.size() == 1) {
                    String usersStr = usersTag.text().replaceAll("\\D", "");
                    int users = Integer.parseInt(usersStr);
                    if (descriptionTag.size() == 1) {
                        String description = replaceBrTags(descriptionTag);
                        return parsedEntity(new Channel(name, title, description, users), descriptionTag);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<ParsedEntity<WebMessage>> parseMessages(@Nonnull String html) {
        Date date = new Date();
        Document document = getDocument(html);
        String channel = extractChannel(document);
        if (channel == null) {
            return Collections.emptyList();
        }
        return document.getElementsByClass("tgme_widget_message").stream()
                .map(messageWidget -> parse(date, channel, messageWidget))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    private ParsedEntity<WebMessage> parse(Date date, String channel, Element messageWidget) {
        try {
            Elements textWidget = messageWidget.getElementsByClass("tgme_widget_message_text");
            if (!isPinnedMessage(textWidget)) {
                WebMessage.WebMessageBuilder builder = WebMessage.builder()
                        .id(messageId(messageWidget))
                        .channel(channel)
                        .type(messageType(messageWidget))
                        .textContent(replaceBrTags(textWidget))
                        .loadDate(date)
                        .publishDate(publishDate(messageWidget))
                        .viewCount(views(messageWidget))
                        .replyToMessageId(replyToMessageId(messageWidget));
                builder = fillForwarding(messageWidget, builder);
                return parsedEntity(builder.build(), textWidget);
            }
            return null;
        } catch (RuntimeException e) {
            log.info(messageWidget.html());
            throw e;
        }
    }

    private boolean isPinnedMessage(Elements textWidget) {
        return textWidget.text().contains("pinned «");
    }

    private <T> ParsedEntity<T> parsedEntity(T entity, Elements content) {
        Set<String> hashTags = new HashSet<>();
        Set<String> links = new HashSet<>();
        Set<String> mentions = new HashSet<>();
        content.select("a").stream()
                .filter(a -> a.attributes().hasKey("href"))
                .forEach(a -> extractMetaInfo(hashTags, links, mentions, a));

        return new ParsedEntity<>(entity, mentions, links, hashTags);
    }

    private void extractMetaInfo(Set<String> hashTags, Set<String> links, Set<String> mentions, Element a) {
        Attributes attributes = a.attributes();
        String text = a.text();
        String href = attributes.get("href");
        if (text.startsWith("#")) {
            hashTags.add(text.substring(1));
        } else if (href.startsWith("http")) {
            if (!text.startsWith("@")) {
                links.add(href);
                String mention = extractLinkMention(href);
                if (mention != null) {
                    mentions.add(mention);
                }
            } else {
                mentions.add(text.substring(1));
            }
        }
    }

    @Nullable
    private String extractChannel(Document document) {
        Elements usernameTag = document.getElementsByClass("tgme_channel_info_header_username");
        if (usernameTag.isEmpty()) {
            return null;
        }
        return usernameTag.text().substring(1);
    }

    private long messageId(Element messageWidget) {
        String messageIdStr = messageWidget.attributes().get("data-post").replaceFirst("^.*?/", "");
        return Long.parseLong(messageIdStr);
    }

    private MessageType messageType(Element messageWidget) {
        Elements videos = messageWidget.getElementsByClass("tgme_widget_message_video");
        Elements photos = messageWidget.getElementsByClass("tgme_widget_message_photo");
        Elements text = messageWidget.getElementsByClass("tgme_widget_message_text");

        if (videos.isEmpty() && photos.isEmpty() && !text.isEmpty()) {
            return MessageType.Text;
        } else if (!videos.isEmpty() && !photos.isEmpty()) {
            return MessageType.Multimedia;
        } else if (!videos.isEmpty()) {
            return MessageType.Video;
        } else if (!photos.isEmpty()) {
            return MessageType.Photo;
        }
        return MessageType.Other;
    }

    @Nullable
    private Date publishDate(Element messageWidget) {
        Elements messageInfoTag = messageWidget.getElementsByClass("tgme_widget_message_info");
        Elements timeTag = messageInfoTag.select("time");
        if (timeTag.size() == 1) {
            String datetimeAttribute = timeTag.get(0).attributes().get("datetime");
            Instant instant = OffsetDateTime.parse(datetimeAttribute).toInstant();
            return Date.from(instant);
        }
        return null;
    }

    private int views(Element messageWidget) {
        Elements viewsTag = messageWidget.getElementsByClass("tgme_widget_message_views");
        String text = viewsTag.text();
        double k = 1.0;
        if (text.endsWith("K")) {
            k = 1000;
        } else if (text.endsWith("M")) {
            k = 1_000_000;
        }

        return (int) (Double.parseDouble(text.replaceAll("[MK]", "")) * k);
    }

    @Nonnull
    private WebMessage.WebMessageBuilder fillForwarding(Element messageWidget, WebMessage.WebMessageBuilder builder) {
        Elements forwardedWidget = messageWidget.getElementsByClass("tgme_widget_message_forwarded_from_name");
        if (forwardedWidget.size() == 1) {
            Attributes attributes = forwardedWidget.get(0).attributes();
            String href = attributes.get("href");
            Matcher matcher = TELEGRAM_CHANNEL_LINK.matcher(href);
            if (matcher.find()) {
                return builder
                        .forwardedFromChannel(matcher.group(1))
                        .forwardedFromMessageId(Long.valueOf(matcher.group(2)));
            }
        }
        return builder;
    }

    private Long replyToMessageId(Element messageWidget) {
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
    private String replaceBrTags(Elements elements) {
        elements.select("br").append("\\n");
        return elements.text().replaceAll("\\\\n", "\n");
    }

    @Nullable
    private String extractLinkMention(String link) {
        Matcher matcher = TELEGRAM_CHANNEL_LINK.matcher(link);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
