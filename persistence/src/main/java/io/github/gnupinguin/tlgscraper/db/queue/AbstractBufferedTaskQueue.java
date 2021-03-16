package io.github.gnupinguin.tlgscraper.db.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBufferedTaskQueue<T extends Task<?, ?>> implements BufferedTaskQueue<T> {

    private static final int DEFAULT_CHATS_COUNT = 20;
    private static final int PROCESSED_OVERHEAD = (int)(DEFAULT_CHATS_COUNT / 2.0 * 3);

    private final TaskQueue<T> taskQueue;

    private final Queue<T> inputQueue = new LinkedList<>();
    private final Map<Object, T> active = new HashMap<>(DEFAULT_CHATS_COUNT);
    private final Lock lock = new ReentrantLock();

    @Override
    @Nullable
    public T poll() {
        lock.lock();
        try {
            fetch();
            if (inputQueue.size() > 0) {
                T task = inputQueue.poll();
                active.put(task.getId(), withStatus(task, TaskStatus.SuccessfullyProcessed));
                return task;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public void add(List<T> tasks) {
        taskQueue.add(tasks.stream()
                .map(task -> withStatus(task, TaskStatus.Initial))
                .collect(Collectors.toList()));
    }

    @Override
    public void markInvalid(T task) {
        updateStatus(task, TaskStatus.InvalidProcessed);
    }

    @Override
    public void markUndefined(T task) {
        updateStatus(task, TaskStatus.Undefined);
    }

    @Override
    public void markFiltered(T task) {
        updateStatus(task, TaskStatus.Filtered);
    }

    private void updateStatus(@Nonnull T task, @Nonnull TaskStatus status) {
        try {
            lock.lock();
            active.compute(task, (id, t) -> withStatus(task, status));
            if (active.size() >= PROCESSED_OVERHEAD) {
                clearProcessedBuffer();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void restore(List<T> tasks) {
        try {
            lock.lock();
            taskQueue.update(tasks.stream()
                    .map(task -> withStatus(task, TaskStatus.Initial))
                    .peek(task -> active.compute(task.getId(), (id, t) -> withStatus(task, TaskStatus.Initial)))
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
        if (!active.isEmpty()) {
            taskQueue.update(active.values());
            active.clear();
        }
    }

    public T withStatus(T task, TaskStatus status) {
        task.setStatus(status);
        return task;
    }

}
