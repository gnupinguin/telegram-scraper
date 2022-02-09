package io.github.gnupinguin.tlgscraper.scraper.scraper.model;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MessageType;
import lombok.*;

import java.util.Date;

@Data
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebMessage {

    private long id;

    private String channel;

    private Long replyToMessageId;

    private String forwardedFromChannel;

    private Long forwardedFromMessageId;

    private MessageType type;

    private String textContent;

    private Date publishDate;

    private int viewCount;

}

