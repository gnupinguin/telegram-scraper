package io.github.gnupinguin.tlgscraper.db.queue.messages;

import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import io.github.gnupinguin.tlgscraper.db.repository.DbTools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessagesTaskQueueImpl implements MessagesTaskQueue {

    private static final String SELECT_QUERY = "SELECT q.internal_message_id, q.status, c.name, m.message_id FROM lock_next_message_task(0, 1, ?) AS q \n" +
            "INNER JOIN message AS m ON q.internal_message_id = m.internal_id\n" +
            "INNER JOIN chat AS c ON m.chat_id = c.id;";

    private static final String INSERT_QUERY = "INSERT INTO message_task_queue(internal_message_id, status) VALUES (?, ?);";

    private static final String SELECT_LOCKED = "SELECT q.internal_message_id, q.status, c.name, m.message_id FROM message_task_queue AS q \n" +
            "INNER JOIN message AS m ON q.internal_message_id = m.internal_id\n" +
            "INNER JOIN chat AS c ON m.chat_id = c.id" +
            "WHERE q.status = 1 LIMIT ?;";
    private static final String UPDATE_STATUS_QUERY = "UPDATE message_task_queue SET status = ? WHERE internal_message_id = ?;";

    private final QueryExecutor queryExecutor;

    @Override
    public List<MessageTask> poll(int count) {
        return queryExecutor.selectQuery(SELECT_QUERY, this::fromFields, List.of(count));
    }

    @Override
    public List<MessageTask> getLocked(int count) {
        return queryExecutor.selectQuery(SELECT_LOCKED, this::fromFields, List.of(count));
    }

    @Override
    public boolean add(Collection<MessageTask> tasks) {
        queryExecutor.batchedUpdateQuery(INSERT_QUERY, this::toFields, tasks, DbTools.ID_GENERATOR_MAPPER);
        return true;
    }

    @Override
    public boolean update(Collection<MessageTask> tasks) {
        queryExecutor.batchedUpdateQuery(UPDATE_STATUS_QUERY,
                task -> List.of(task.getStatus().getStatus(), task.getInternalMessageId()),
                tasks, null);
        return true;
    }

    private MessageTask fromFields(List<Object> fields) {
        return MessageTask.builder()
                .internalMessageId((Long) fields.get(0))
                .status(getTaskStatus(fields.get(1)))
                .messageTaskInfo(MessageTask.MessageTaskInfo.builder()
                        .channel((String) fields.get(2))
                        .beforeMessageId((Long) fields.get(3))
                        .build())
                .build();
    }

    private List<Object> toFields(MessageTask task) {
        return List.of(task.getInternalMessageId(), task.getStatus().getStatus());
    }

    @Nonnull
    private TaskStatus getTaskStatus(Object field) {
        return TaskStatus.valueOf((int) field);
    }

}
