package io.github.gnupinguin.tlgscraper.db.orm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryExecutorImpl implements QueryExecutor {

    private final DbManager dbManager;

    @Override
    public <T, R> List<R> batchedUpdateQuery(@Nonnull String query,
                                             @Nonnull Function<T, List<Object>> objectSerializer,
                                             @Nonnull Collection<T> objects,
                                             @Nullable Function<List<Object>, R> objectDeserializer) {
            return dbManager.executeBatched(query,
                    statement -> insertObjectsToStatement(objects, objectSerializer, statement),
                    rs -> mapSelectionToObjects(objectDeserializer, rs));
    }

    @Override
    public @Nonnull <T> List<T> selectQuery(String query, Function<List<Object>, T> rowMapper, Collection<?> substitutions) {
        return dbManager.execute(query,
                statement -> fillSubstitutionsStatement(substitutions, statement),
                rs -> mapSelectionToObjects(rowMapper, rs));
    }

    @Override
    public java.sql.Date getCurrentDate() {
        var sqlQuery = "SELECT NOW();";
        return dbManager.execute(sqlQuery,
                statement -> {},
                this::getDate);
    }

    private Date getDate(ResultSet rs) {
        try {
            if (rs.next()) {
                return rs.getDate(1);
            }
            return null;
        } catch (SQLException e) {
            log.info("Could not connect to db", e);
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    private <T> List<T> mapSelectionToObjects(@Nullable Function<List<Object>, T> objectDeserializer, ResultSet resultSet) {
        var result = new ArrayList<T>();
        if (objectDeserializer != null) {
            try {
                int rowSize = getRowSize(resultSet);
                while (resultSet.next()) {
                    var fields = new ArrayList<>();
                    for (int i = 0; i < rowSize; i++) {
                        var param = extractParamFromSelection(resultSet, i);
                        fields.add(param);
                    }
                    result.add(objectDeserializer.apply(fields));
                }
            } catch (SQLException e) {
                log.info("Could not iterate by result", e);
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private int getRowSize(ResultSet resultSet) throws SQLException {
        return resultSet.getMetaData().getColumnCount();
    }

    private Object extractParamFromSelection(ResultSet resultSet, int i) {
        Object param;
        try {
            param = resultSet.getObject(i + 1);
        } catch (SQLException e) {
            log.info("Can not extract field for entity. Index {}. Exception: {}", i + 1, e);
            throw new RuntimeException(e);//TODO add new exception
        }
        return param;
    }

    private void fillSubstitutionsStatement(Collection<?> substitutions, PreparedStatement statement) {
        int i = 1;
        for (Object substitution : substitutions) {
            setObject(i++, substitution, statement);
        }
    }

    private <T> void insertObjectsToStatement(Collection<T> objects, Function<T, List<Object>> rowMapper, PreparedStatement statement) {
        for (T object : objects) {
            try {
                List<Object> fields = rowMapper.apply(object);
                for (int j = 0; j < fields.size(); j++) {
                    setObject(j + 1, fields.get(j), statement);
                }
                tryBatch(statement, object);
            } catch (Exception e) {
                log.info("Insertion failed for object: {}", object);
                throw new RuntimeException(e);
            }
        }
    }

    private void setObject(int i, Object object, PreparedStatement statement){
        try {
            statement.setObject(i, object);
        } catch (SQLException e) {
            log.info("Could not insert object {} to position {}", object, i);
            throw new RuntimeException(e);
        }
    }

    private void tryBatch(PreparedStatement statement, Object object) {
       try {
           statement.addBatch();
       } catch (SQLException e) {
           log.info("Can not add batch for object {}", object);
           throw new RuntimeException(e); //TODO add new exception
       }
    }

}
