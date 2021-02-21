package io.github.gnupinguin.tlgscraper.db.orm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbManager {

    private final DbProperties properties;

    //do not forget use batches in onStatement
    public <T> T executeBatched(String sqlQuery,
                                Consumer<PreparedStatement> onStatement,
                                Function<ResultSet, T> resultMapper) {
        try (var connection = openConnection()) {
            connection.setAutoCommit(false);
            try (var statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
                onStatement.accept(statement);
                statement.executeBatch();
                connection.commit();
                try (var resultSet = statement.getGeneratedKeys()) {
                    return resultMapper.apply(resultSet);
                }
            }
        } catch (SQLException e) {
            log.info("Exception happened during SQL batched execution: {}", sqlQuery, e);
            throw new RuntimeException();//TODO add new exception
        }
    }

    public <T> T execute(String sqlQuery,
                         Consumer<PreparedStatement> onStatement,
                         Function<ResultSet, T> resultMapper) {
        try (var connection = openConnection()) {
            try (var statement = statement(sqlQuery, connection)) {
                onStatement.accept(statement);
                try (var resultSet = statement.executeQuery()) {
                    return resultMapper.apply(resultSet);
                }
            }
        } catch (SQLException e) {
            log.info("Exception happened during SQL execution: {}", sqlQuery, e);
            throw new RuntimeException();//TODO add new exception
        }
    }

    private PreparedStatement statement(String sqlQuery, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        preparedStatement.setQueryTimeout(60);
        return preparedStatement;
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
    }

}
