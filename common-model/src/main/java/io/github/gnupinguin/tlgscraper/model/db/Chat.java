package io.github.gnupinguin.tlgscraper.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    private Long id;

    private String name;

    private String description;

    private Integer members;

}
