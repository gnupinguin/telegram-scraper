package io.github.gnupinguin.tlgscraper.db.queue.mention;

import io.github.gnupinguin.tlgscraper.db.queue.Task;
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

@Data
@AllArgsConstructor
public class MentionTask implements Task<String, String> {

    @With
    private TaskStatus status;

    private String name;

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getEntity() {
        return name;
    }

}
