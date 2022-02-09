package io.github.gnupinguin.tlgscraper.scraper.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Arrays;

@Data
@AllArgsConstructor
@Table("mention_queue")
public class MentionTask {

    @Id
    private String name;

    @With
    private TaskStatus status;

    public enum TaskStatus {
        Initial(0),
        Active(1),
        SuccessfullyProcessed(2),
        InvalidProcessed(3),
        Filtered(4),
        Undefined(-1);

        private final int status;

        TaskStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public static TaskStatus valueOf(int status) {
            return Arrays.stream(TaskStatus.values())
                    .filter(taskStatus -> taskStatus.status == status)
                    .findFirst()
                    .orElse(Undefined);
        }

    }

}
