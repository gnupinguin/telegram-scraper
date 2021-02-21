package io.github.gnupinguin.tlgscraper.model.scraper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageInfo {

    private long id;

    private long chatId;

    private Long replyToMessageId;

    private Long forwardedFromChatId;

    private Long forwardedFromMessageId;

    private MessageType type;

    private String textContent;

    private Date publishDate;

    private Date loadDate;

    private int viewCount;

    private List<String> links;

    private List<String> mentions;

    private List<String> hashTags;

}

