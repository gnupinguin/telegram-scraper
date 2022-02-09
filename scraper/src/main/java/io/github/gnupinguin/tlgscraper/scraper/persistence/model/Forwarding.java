package io.github.gnupinguin.tlgscraper.scraper.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Forwarding {

    private Long id;

    private String forwardedFromChannel;

    private Long forwardedFromMessageId;

    private Message message;

}
