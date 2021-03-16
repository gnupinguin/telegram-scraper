package io.github.gnupinguin.tlgscraper.db.queue.mention;

import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutor;
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MentionTaskQueueImpl implements MentionTaskQueue {

    private static final String SELECT_QUERY = "SELECT status, name FROM lock_next_mention(0, 1, ?)";

    private static final String INSERT_QUERY = "INSERT INTO mention_queue(name) VALUES (?) " +
            "ON CONFLICT (name) DO NOTHING;";

    private static final String SELECT_LOCKED = "SELECT status, name FROM mention_queue WHERE status = 1 LIMIT ?;";
    private static final String UPDATE_STATUS_QUERY = "UPDATE mention_queue SET status = ? WHERE name = ?;";

    private final QueryExecutor queryExecutor;

    @Override
    public List<MentionTask> poll(int count) {
        return queryExecutor.selectQuery(SELECT_QUERY, this::map, List.of(count));
    }

    @Override
    public List<MentionTask> getLocked(int count) {
        return queryExecutor.selectQuery(SELECT_LOCKED, this::map, List.of(count));
    }

    @Override
    public boolean add(Collection<MentionTask> tasks) {
        queryExecutor.batchedUpdateQuery(INSERT_QUERY, t -> List.of(t.getName()), tasks, null);
        return true;
    }

    @Override
    public boolean update(Collection<MentionTask> tasks) {
        queryExecutor.batchedUpdateQuery(UPDATE_STATUS_QUERY,
                task -> List.of(task.getStatus().getStatus(), task.getName()),
                tasks, null);
        return true;
    }

    private MentionTask map(List<Object> fields) {
        return new MentionTask(getTaskStatus(fields), (String) fields.get(1));
    }

    @Nonnull
    private TaskStatus getTaskStatus(List<Object> fields) {
        return TaskStatus.valueOf((int) fields.get(0));
    }

}
