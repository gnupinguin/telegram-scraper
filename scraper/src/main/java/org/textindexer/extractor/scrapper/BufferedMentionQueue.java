package org.textindexer.extractor.scrapper;

import io.github.gnupinguin.tlgscraper.db.queue.MentionTask;
import io.github.gnupinguin.tlgscraper.db.queue.StatusTaskQueue;
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BufferedMentionQueue implements MentionQueue {

    private static final int DEFAULT_CHATS_COUNT = 20;
    private static final int PROCESSED_OVERHEAD = (int)(DEFAULT_CHATS_COUNT / 2.0 * 3);

    private final StatusTaskQueue<MentionTask> taskQueue;

    private final Queue<MentionTask> inputQueue = new LinkedList<>();
    private final Map<String, MentionTask> processed = new HashMap<>(DEFAULT_CHATS_COUNT);
    private final Lock lock = new ReentrantLock();

    @Override
    @Nullable
    public String poll() {
        try {
            lock.lock();
            fetch();
            if (inputQueue.size() > 0) {
                MentionTask task = inputQueue.poll();
                processed.put(task.getName(), new MentionTask(TaskStatus.SuccessfullyProcessed, task.getName()));
                return task.getName();
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public void add(List<String> mentions) {
        taskQueue.add(mentions.stream()
                .map(m -> new MentionTask(TaskStatus.Initial, m))
                .collect(Collectors.toList()));
    }

    @Override
    public void markInvalid(String mention) {
        updateStatus(mention, TaskStatus.InvalidProcessed);
    }

    @Override
    public void markUndefined(String mention) {
        updateStatus(mention, TaskStatus.Undefined);
    }

    private void updateStatus(String mention, TaskStatus status) {
        try {
            lock.lock();
            processed.compute(mention, (m, task) -> new MentionTask(status, mention));
            if (processed.size() >= PROCESSED_OVERHEAD) {
                clearProcessedBuffer();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void restore(List<String> failedMentions) {
        try {
            lock.lock();
            taskQueue.updateStatuses(failedMentions.stream()
                    .map(m -> new MentionTask(TaskStatus.Initial, m))
                    .peek(mention -> processed.compute(mention.getName(), (m, task) -> new MentionTask(TaskStatus.Initial, m)))
                    .collect(Collectors.toList()));
        } finally {
            lock.unlock();
        }
    }

    private void fetch() {
        if (inputQueue.isEmpty()) {
            clearProcessedBuffer();
            inputQueue.addAll(taskQueue.poll(DEFAULT_CHATS_COUNT));
            log.info("Fetched chats count: {}", inputQueue.size());
        }
    }

    private void clearProcessedBuffer() {
        if (!processed.isEmpty()) {
            taskQueue.updateStatuses(processed.values());
            processed.clear();
        }
    }

}
