package io.github.gnupinguin.analyzer.datasource;

import io.github.gnupinguin.analyzer.configuration.Profiles;
import io.github.gnupinguin.analyzer.configuration.SparkDbConfiguration;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Properties;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.date_format;

@Profile({Profiles.REAL_DATA})
@Component
@RequiredArgsConstructor
public class MessagesSparkDataSource implements SparkDataSource {

    private final SparkSession spark;
    private final SparkDbConfiguration configuration;

    @Override
    public Dataset<Row> load() {
        return loadDate("2021-03-11", 200_000);
    }

    private Dataset<Row> load(String table) {
        Properties properties = new Properties();
        properties.put("user", "scraper");
        properties.put("password", "Scraper_db!");
        properties.put("driver", "org.postgresql.Driver");
        properties.put("fetchsize", "5000");
        return spark.read()
                .jdbc("jdbc:postgresql://localhost:5433/scraperdb", table, properties);
    }

    private Dataset<Row> basicLoad() {
        var chat = load("chat").select(
                col("id").as("chat_id"),
                col("name").as("channel"),
                col("members")
        );

        var message = load("message");
        return chat.join(message, chat.col("chat_id").$eq$eq$eq(message.col("chat_id")))
                .where(col("message_id").notEqual(-1))
                .where(col("type").notEqual(-1))
                .where(col("members").geq(10_000))
                .drop("chat_id");
    }

    private Dataset<Row> loadLimited(int n) {
        return basicLoad().limit(1000);
    }

    private Dataset<Row> loadChannel(String channel) {
        return basicLoad().where(col("channel").equalTo(channel.toLowerCase()));
    }

    private Dataset<Row> loadDate(String dayDate, int limit) {
        return basicLoad()
                .where(date_format(col("publish_date"), "yyyy-MM-dd").equalTo(dayDate))
                .limit(limit);
    }

}
