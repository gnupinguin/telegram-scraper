DROP TABLE IF EXISTS message_task_queue;
CREATE TABLE message_task_queue(
    internal_message_id bigint NOT NULL,
    status integer not null default 0, -- 0 - not processed, 1 - start processing, 2 - finished
    PRIMARY KEY(internal_message_id)
);

CREATE INDEX message_task_queue_status_idx on message_task_queue (status);

CREATE OR REPLACE FUNCTION lock_next_message_task(actualStatus int, newStatus int, total int)
    RETURNS TABLE (id bigint, internal_message_id bigint, status int) AS
$func$
BEGIN
    RETURN QUERY
        WITH next_message_task AS (SELECT m.internal_message_id FROM message_task_queue AS m
                              WHERE m.status = actualStatus LIMIT total FOR UPDATE skip locked)
            UPDATE message_task_queue
                SET status = newStatus FROM next_message_task
                WHERE message_task_queue.internal_message_id = next_message_task.internal_message_id
                RETURNING message_task_queue.internal_message_id, message_task_queue.internal_message_id, message_task_queue.status;
end; $func$ LANGUAGE plpgsql;


