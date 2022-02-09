package io.github.gnupinguin.tlgscraper.scraper.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Replying {

    @Id
    private Long replyToMessageId;

    private Message message;

}
