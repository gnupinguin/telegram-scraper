package io.github.gnupinguin.analyzer;

import io.github.gnupinguin.analyzer.document.Document;
import io.github.gnupinguin.analyzer.document.MessageDocumentProcessor;
import io.github.gnupinguin.analyzer.entity.Msg;
import io.github.gnupinguin.analyzer.search.DocumentSearcher;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import scala.Tuple2;

import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);

        SparkSession spark = context.getBean(SparkSession.class);
        MessageDocumentProcessor documentProcessor = context.getBean(MessageDocumentProcessor.class);
        DocumentSearcher documentSearcher = context.getBean(DocumentSearcher.class);

//        searcher.search()

//        var properties = new Properties();
//        properties.put("user", "scraper");
//        properties.put("password", "Scraper_db!");
//        properties.put("Driver", "org.postgresql.Driver");
//        properties.put("fetchsize", "400");
//
//        var messages = spark.read()
//                .jdbc("jdbc:postgresql://localhost:5433/scraperdb", "message", properties)
//                .where(col("chat_id").equalTo(52).and(col("message_id").notEqual(-1)));


//        HashingTF hashingTF = new HashingTF()
//                .setInputCol("filtered")
//                .setOutputCol("rawFeatures")
//                .setNumFeatures(4096);


        var data = spark.createDataFrame(List.of(
                RowFactory.create(1L, 1L, 1L, "Это пост Навального!", date(), date(), 1, 2),
                RowFactory.create(1L, 2L, 2L, "Этот пост купил Путин!", date(), date(), 1, 2),
                RowFactory.create(1L, 3L, 3L, "Обычный текст", date(), date(), 1, 2)
        ), new StructType(new StructField[]{
                new StructField("chat_id", DataTypes.LongType, false, Metadata.empty()),
                new StructField("message_id", DataTypes.LongType, false, Metadata.empty()),
                new StructField("internal_message_id", DataTypes.LongType, false, Metadata.empty()),
                new StructField("text_content", DataTypes.StringType, false, Metadata.empty()),
                new StructField("publish_date", DataTypes.DateType, false, Metadata.empty()),
                new StructField("load_date", DataTypes.DateType, false, Metadata.empty()),
                new StructField("type", DataTypes.IntegerType, false, Metadata.empty()),
                new StructField("view_count", DataTypes.IntegerType, false, Metadata.empty())
        }));

        var documents = documentProcessor.map(data);

        documents.getDocuments().foreach((ForeachFunction<Document<Msg>>) d -> System.out.println(d));

        documentSearcher
                .search(documents, "пост", 2)
                .foreach( (ForeachFunction<Tuple2<Double, Document<Msg>>>)  d -> System.out.println(d));

    }

    private static java.sql.Date date() {
        return new java.sql.Date(System.currentTimeMillis());
    }

}
