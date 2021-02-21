package io.github.gnupinguin.tlgscraper.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private Long internalId;

    private Long chatId;

    private Long messageId;

    private Long replyToMessageId;

    private Long forwardedFromChatId;

    private Long forwardedFromMessageId;

    private Integer type;

    private String textContent;

    private Timestamp publishDate;

    private Timestamp loadDate;

    private Integer views;

}
