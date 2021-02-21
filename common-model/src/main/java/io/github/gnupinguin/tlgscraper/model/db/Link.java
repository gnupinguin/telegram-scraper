package io.github.gnupinguin.tlgscraper.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    private Long id;

    private Long internalMessageId;

    private String url;

}
