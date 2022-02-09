package io.github.gnupinguin.tlgscraper.scraper.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Set;

@Data
@ToString(exclude = {"mentions", "hashTags", "links", "forwarding", "replying"})
@Builder
@AllArgsConstructor
public class Message {

    @Id
    private Long internalId;

    private Channel channel;

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
