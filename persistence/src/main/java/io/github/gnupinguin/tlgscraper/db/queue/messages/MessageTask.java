package io.github.gnupinguin.tlgscraper.db.queue.messages;

import io.github.gnupinguin.tlgscraper.db.queue.Task;
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class MessageTask implements Task<Long, MessageTask.MessageTaskInfo> {

    private long internalMessageId;

    @With
    private TaskStatus status;

    private MessageTaskInfo messageTaskInfo;

    @Override
    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public MessageTaskInfo getEntity() {
        return messageTaskInfo;
    }

    @Override
    public Long getId() {
        return internalMessageId;
    }

    @Data
    @Builder
    @RequiredArgsConstructor
    public static class MessageTaskInfo {

        private final String channel;

        private final long beforeMessageId;

    }

}
