package io.github.gnupinguin.tlgscraper.model.db;

import io.github.gnupinguin.tlgscraper.model.scraper.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.Set;

@Data
@ToString(exclude = {"mentions", "hashTags", "links", "forwarding", "replying"})
@Builder
@AllArgsConstructor
public class Message {

    private Long internalId;

    private Chat channel;

    private long id;

    private MessageType type;

    private String textContent;

    private Date publishDate;

    private Date loadDate;

    private int viewCount;

    private Forwarding forwarding;

    private Replying replying;

    private Set<Mention> mentions;

    private Set<HashTag> hashTags;

    private Set<Link> links;

}
