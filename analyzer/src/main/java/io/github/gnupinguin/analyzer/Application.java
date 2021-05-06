package io.github.gnupinguin.analyzer;

import io.github.gnupinguin.analyzer.datasource.SparkDataSource;
import io.github.gnupinguin.analyzer.tokenizer.TopicPipeline;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
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

import java.util.List;

//Spark dependencies load old gson version.
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
@ConfigurationPropertiesScan
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);

        SparkSession spark = context.getBean(SparkSession.class);
        SparkDataSource sparkDataSource = context.getBean(SparkDataSource.class);

//        createData(sparkDataSource.load(), spark);

//        Dataset<Row> load = spark.read().load("/home/gnupinguin/Projects/NLP_TRAIN");
//        load.show();

//        createData(testData(spark), spark);
        createData(sparkDataSource.load(), spark);
    }

    private static void createData(Dataset<Row> data, SparkSession spark) {
        TopicPipeline ldaPipeline = new TopicPipeline(spark);
        data.persist();

        Dataset<Row> result = ldaPipeline.apply(data);
        result.show();
//        result.write().mode(SaveMode.Overwrite).save("/home/gnupinguin/Projects/NLP_TRAIN");
    }

    private static java.sql.Date date() {
        return new java.sql.Date(System.currentTimeMillis());
    }

    private static Dataset<Row> testData(SparkSession spark) {
        return spark.createDataFrame(List.of(
                RowFactory.create("Это пост Навального!"),
                RowFactory.create("Полиция в Петербурге разогнала митинг сторонников Навального."),
                RowFactory.create("Покупайте холодильники, лучшие в городе! Бесплатная доставка, приемлимые цены!"),
                RowFactory.create("В тренировке парада в Петербурге примут участие более 4 тыс. военнослужащих"),
                RowFactory.create("Продам собаку. Дорого"),
                RowFactory.create("Экспертиза показала, что здание Биржи Петербурга находится в критическом состоянии"),
                RowFactory.create("Новая выставка в галерее свиное рыло. Приходите, будет интересно"),
                RowFactory.create("На митинге выступали в поддержку Путина, и кричали, что Навальный вор."),
                RowFactory.create("Студенты пришли на очередную несогласованную акцию в поддержу Навального."),
                RowFactory.create("Учащиеся школы номер два были приглашены на выставку в Русский музей")
        ), new StructType(new StructField[]{
                new StructField("text_content", DataTypes.StringType, false, Metadata.empty())
        }));
    }
}
