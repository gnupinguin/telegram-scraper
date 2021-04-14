package io.github.gnupinguin.analyzer.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Msg implements Serializable {
    private final Long internalId;

    private final Long channelId;

    private final Long id;

    private final Integer type;

    private final String textContent;

    private final Date publishDate;

    private final Date loadDate;

    private final Integer viewCount;
}
