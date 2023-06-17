package io.github.gnupinguin.tlgscraper.db.queue;

import java.util.Arrays;

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
