package io.github.gnupinguin.tlgscraper.db.queue.mention;

import io.github.gnupinguin.tlgscraper.db.queue.AbstractBufferedTaskQueue;
import io.github.gnupinguin.tlgscraper.db.queue.TaskQueue;
import org.springframework.stereotype.Component;

@Component
public class BufferedMentionTaskQueueImpl extends AbstractBufferedTaskQueue<MentionTask> implements BufferedMentionTaskQueue {
    public BufferedMentionTaskQueueImpl(TaskQueue<MentionTask> taskQueue) {
        super(taskQueue);
    }
}
