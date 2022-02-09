package io.github.gnupinguin.tlgscraper.scraper.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;



@Data
@ToString(exclude = {"messages", "description"})
@Builder
@AllArgsConstructor
public class Channel {

    private Long id;

    private String name;

    private String title;

    private String description;

    private Integer members;

    private List<Message> messages;

}
