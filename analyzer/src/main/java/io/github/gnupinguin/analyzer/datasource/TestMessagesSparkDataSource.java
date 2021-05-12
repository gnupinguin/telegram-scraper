package io.github.gnupinguin.analyzer.datasource;

import io.github.gnupinguin.analyzer.configuration.Profiles;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile({Profiles.TEST_DATA})
@Component
@RequiredArgsConstructor
public class TestMessagesSparkDataSource implements SparkDataSource {

    private final SparkSession spark;

    @Override
    public Dataset<Row> load() {
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
                RowFactory.create("Учащиеся школы номер два были приглашены на выставку в Русский музей"),
                RowFactory.create("В Улан-Удэ прошли мероприятия по подавлению митингов.")
        ), new StructType(new StructField[]{
                new StructField("text_content", DataTypes.StringType, false, Metadata.empty())
        }));
    }

}
