package io.github.gnupinguin.tlgscraper.db.queue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentionTask implements StatusTask{

    private TaskStatus status;

    private String name;

}
