package io.github.gnupinguin.tlgscraper.scraper.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mention {

    private Long id;

    private String channelName;

    private Message message;

}
