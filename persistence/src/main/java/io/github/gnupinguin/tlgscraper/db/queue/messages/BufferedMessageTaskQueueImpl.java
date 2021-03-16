package io.github.gnupinguin.tlgscraper.db.queue.messages;

import io.github.gnupinguin.tlgscraper.db.queue.AbstractBufferedTaskQueue;
import io.github.gnupinguin.tlgscraper.db.queue.TaskQueue;
import org.springframework.stereotype.Component;

@Component
public class BufferedMessageTaskQueueImpl extends AbstractBufferedTaskQueue<MessageTask> implements BufferedMessageTaskQueue {
    public BufferedMessageTaskQueueImpl(TaskQueue<MessageTask> taskQueue) {
        super(taskQueue);
    }
}
