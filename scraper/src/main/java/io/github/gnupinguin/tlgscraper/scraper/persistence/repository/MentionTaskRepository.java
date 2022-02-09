package io.github.gnupinguin.tlgscraper.scraper.persistence.repository;

import io.github.gnupinguin.tlgscraper.scraper.persistence.model.MentionTask;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MentionTaskRepository extends CrudRepository<MentionTask, String> {

    @Query("SELECT * FROM lock_next_mention(0, 1, :count)")
    List<MentionTask> getNext(@Param("count") int count);

    //TODO check insertion work without ON CONFLICT
    //    private static final String INSERT_QUERY = "INSERT INTO mention_queue(name) VALUES (?) " +
    //            "ON CONFLICT (name) DO NOTHING;";

    @Query("SELECT * FROM mention_queue WHERE status = 1 LIMIT :count")
    List<MentionTask> getLocked(@Param("count") int count);


}
